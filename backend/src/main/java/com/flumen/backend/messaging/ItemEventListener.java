package com.flumen.backend.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import flumen.events.DomainEvent;
import com.flumen.backend.services.ItemEventProcessor;

@Component
public class ItemEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ItemEventListener.class);
    private final ItemEventProcessor eventProcessor;

    public ItemEventListener(ItemEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @RabbitListener(queues = "${rabbitmq.queue.item-events}")
    public void handleItemEvent(DomainEvent event) {
        logger.info("Received event from RabbitMQ: {} for item: {}", 
            event.getEventType(), event.getEntityId());
        
        eventProcessor.process(event)
            .exceptionally(throwable -> {
                logger.error("Failed to process event from RabbitMQ: {}", 
                    event.getEventId(), throwable);
                // Here you could implement retry logic or dead letter queue
                return null;
            });
    }
}