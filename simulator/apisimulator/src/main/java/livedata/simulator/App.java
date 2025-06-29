package livedata.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import flumen.events.DomainEvent;
import flumen.events.ItemCreatedEvent;
import flumen.events.ItemPositionChangedEvent;
import flumen.events.LocationCreatedEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String MODE = System.getenv().getOrDefault("SIMULATION_MODE", "api"); // "api" or "rabbit"
    private static final String RABBIT_HOST = "localhost";
    private static final String RABBIT_QUEUE = "sorting-events";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Channel rabbitChannel;

    public static void main(String[] args) {
        try {
            if (MODE.equalsIgnoreCase("rabbit")) {
                setupRabbit();
            }
            List<String> items = new ArrayList<>();
            List<String> locations = new ArrayList<>();

            // Step 1: Create 1000 Items
            for (int i = 1; i <= 10; i++) {
                String itemName = "Item" + i;
                sendEvent(new ItemCreatedEvent(itemName, itemName, 1.0, true, new HashMap<>()), "POST");
                items.add(itemName);
            }

            // Step 2: Create 1000 Locations
            for (int i = 1; i <= 10; i++) {
                String locationName = "Location" + i;
                sendEvent(new LocationCreatedEvent(locationName, locationName, true, 0.0, 0.0, 10.0, 1.0, "conveyor", new HashMap<>()), "POST");
                locations.add(locationName);
            }

            // Step 3: Establish Initial Connections
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                String location = locations.get(i % locations.size()); // Distribute items across locations
                sendEvent(new ItemPositionChangedEvent(item, location), "POST");
            }

            // Step 4: Create Connections Between Locations
            for (int i = 0; i < locations.size(); i++) {
                String location1 = locations.get(i);
                String location2 = locations.get((i + 1) % locations.size()); // Connect each location to the next
                createLocationConnection(location1, location2);
            }

            // Step 5: Move Items in a Loop
            while (true) {
                Collections.rotate(locations, 1);

                for (int i = 0; i < items.size(); i++) {
                    String item = items.get(i);
                    String newLocation = locations.get(i);
                    sendEvent(new ItemPositionChangedEvent(item, newLocation), "PUT");
                }

                // Optional: Add a delay to avoid overwhelming the server
                Thread.sleep(1000); // 1-second delay
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }

    private static void setupRabbit() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT_HOST);
        Connection connection = factory.newConnection();
        rabbitChannel = connection.createChannel();
        rabbitChannel.queueDeclare(RABBIT_QUEUE, false, false, false, null);
    }

    private static void sendEvent(DomainEvent event, String httpMethod) throws Exception {
        String json = objectMapper.writeValueAsString(event);
        if (MODE.equalsIgnoreCase("rabbit")) {
            rabbitChannel.basicPublish("", RABBIT_QUEUE, null, json.getBytes());
            logger.info(() -> "Sent event to RabbitMQ: " + json);
        } else {
            String endpoint = getEndpointForEvent(event);
            if (endpoint == null) {
                logger.warning(() -> "Unknown event type: " + event.getClass().getName());
                return;
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + endpoint))
                    .header("Content-Type", "application/json");

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(json);

            if ("POST".equalsIgnoreCase(httpMethod)) {
                requestBuilder.POST(bodyPublisher);
            } else if ("PUT".equalsIgnoreCase(httpMethod)) {
                requestBuilder.PUT(bodyPublisher);
            } else {
                logger.warning("Unsupported HTTP method: " + httpMethod);
                return;
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info(() -> "Sent event to API: " + json);
            } else {
                logger.warning(() -> "Failed to send event to API: " + response.body());
            }
        }
    }

    private static String getEndpointForEvent(DomainEvent event) {
        if (event instanceof ItemCreatedEvent) {
            return "/items";
        } else if (event instanceof LocationCreatedEvent) {
            return "/locations";
        } else if (event instanceof ItemPositionChangedEvent) {
            return "/positions";
        }
        return null;
    }

    private static void createLocationConnection(String locationId1, String locationId2) throws Exception {
        logger.info(() -> String.format("Creating connection between location %s and location %s", locationId1, locationId2));

        String requestBody = String.format("{\"location1Id\":\"%s\",\"location2Id\":\"%s\"}", locationId1, locationId2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/connections"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Location connection created successfully.");
        } else {
            logger.warning(() -> "Failed to create location connection: " + response.body());
        }
    }
}