package livedata.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
            List<String> items = new ArrayList<>();
            List<String> locations = new ArrayList<>();

            // Step 1: Create 1000 Items
            for (int i = 1; i <= 1000; i++) {
                String itemName = "Item" + i;
                createItem(itemName);
                items.add(itemName);
            }

            // Step 2: Create 1000 Locations
            for (int i = 1; i <= 1000; i++) {
                String locationName = "Location" + i;
                createLocation(locationName);
                locations.add(locationName);
            }

            // Step 3: Establish Initial Connections
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                String location = locations.get(i % locations.size()); // Distribute items across locations
                createConnection(item, location);
            }

            // Step 4: Move Items in a Loop
            while (true) {
                for (int i = 0; i < items.size(); i++) {
                    String item = items.get(i);
                    String newLocation = locations.get((i + 1) % locations.size()); // Move to the next location
                    moveConnection(item, newLocation);
                }

                // Optional: Add a delay to avoid overwhelming the server
                Thread.sleep(1000); // 1-second delay
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }

    private static void createItem(String itemName) throws Exception {
        logger.info(() -> "Creating item " + itemName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString(String.format("{\"id\":\"%s\", \"name\":\"%s\"}", itemName, itemName)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info(() -> "Item " + itemName + " created successfully.");
        } else {
            logger.warning(() -> "Failed to create item " + itemName + ": " + response.body());
        }
    }

    private static void createLocation(String locationName) throws Exception {
        logger.info(() -> "Creating location " + locationName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/locations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers
                        .ofString(String.format("{\"id\":\"%s\",\"name\":\"%s\", \"latitude\":0.0,\"longitude\":0.0}",
                                locationName, locationName)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info(() -> "Location " + locationName + " created successfully.");
        } else {
            logger.warning(() -> "Failed to create location " + locationName + ": " + response.body());
        }
    }

    private static void createConnection(String itemId, String locationId) throws Exception {
        logger.info(() -> String.format("Creating connection between item %s and location %s", itemId, locationId));

        String requestBody = String.format("{\"itemId\":\"%s\",\"locationId\":\"%s\"}", itemId, locationId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/positions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Connection created successfully.");
        } else {
            logger.warning(() -> "Failed to create connection: " + response.body());
        }
    }

    private static void moveConnection(String itemId, String newLocationId) throws Exception {
        logger.info(() -> String.format("Moving item %s to new location %s.", itemId, newLocationId));

        String requestBody = String.format("{\"itemId\":\"%s\",\"locationId\":\"%s\"}", itemId, newLocationId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "/positions"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("Item moved successfully.");
        } else {
            logger.warning(() -> "Failed to move item: " + response.body());
        }
    }
}
