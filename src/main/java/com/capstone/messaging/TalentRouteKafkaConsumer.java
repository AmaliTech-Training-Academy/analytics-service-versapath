package com.capstone.messaging;

import com.capstone.exception.TalentRouteProcessingException;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.service.TalentRouteSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TalentRouteKafkaConsumer {

    private final TalentRouteSnapshotService talentRouteSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_TALENT_ROUTE_DLT_TOPIC:talentRoute.create.dlt}")
    private String talentRouteDltTopic;

    @KafkaListener(topics = "${KAFKA_TALENT_ROUTE_TOPIC:talentRoute.create}")
    @Retryable(
            retryFor = {TalentRouteProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenTalentRouteCreate(
            @Payload TalentRouteEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received talent route event from topic: {}, partition: {}, offset: {}, routeId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateTalentRouteEvent(event);

            // Process the talent route event
            TalentRouteSnapshot processedRoute = talentRouteSnapshotService.processTalentRouteEvent(event);

            log.info("Successfully processed talent route event for routeId: {}, internal ID: {}, tracks: {}",
                    event.getId(), processedRoute.getId(),
                    event.getGrowthTracks() != null ? event.getGrowthTracks().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (TalentRouteProcessingException e) {
            log.error("Failed to process talent route event for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        } catch (Exception e) {
            log.error("Unexpected error processing talent route event for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        }
    }

    /**
     * Comprehensive validation of TalentRouteEvent
     */
    private void validateTalentRouteEvent(TalentRouteEvent event) {
        log.debug("Validating talent route event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new TalentRouteProcessingException("Talent route event cannot be null");
        }

        if (event.getId() == null) {
            throw new TalentRouteProcessingException("Talent route event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new TalentRouteProcessingException("Talent route event must contain a valid name");
        }
        log.debug("Talent route event validation successful for routeId: {}", event.getId());
    }

    /**
     * Handle processing failures with DLT support
     */
    private void handleProcessingFailure(TalentRouteEvent event, Acknowledgment acknowledgment, String operationType) {
        String routeId = event.getId().toString();

        log.error("Processing failed for talent route {} operation with routeId: {}. Sending to DLT topic: {}",
                operationType, routeId, talentRouteDltTopic);

        try {
            // Create enhanced event with operation type for DLT analysis
            Map<String, Object> dltPayload = Map.of(
                    "originalEvent", event,
                    "operationType", operationType,
                    "failureTimestamp", System.currentTimeMillis(),
                    "routeId", routeId
            );

            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(talentRouteDltTopic, routeId, dltPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed talent route {} event to DLT for routeId: {}",
                                    operationType, routeId);
                        } else {
                            log.error("Failed to send talent route {} event to DLT for routeId: {}",
                                    operationType, routeId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send talent route {} message to DLT for routeId: {}. Message will be retried by Kafka",
                    operationType, routeId, dltException);
        }
    }

    @KafkaListener(topics = "${KAFKA_TALENT_ROUTE_UPDATE_TOPIC:talentRoute.update}")
    @Retryable(
            retryFor = {TalentRouteProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenTalentRouteUpdate(
            @Payload TalentRouteEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received talentRoute.update event from topic: {}, partition: {}, offset: {}, routeId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateTalentRouteEvent(event);

            // Process the talent route update using existing service logic
            TalentRouteSnapshot updatedRoute = talentRouteSnapshotService.processTalentRouteEvent(event);

            log.info("Successfully updated talent route for routeId: {}, internal ID: {}, tracks: {}",
                    event.getId(), updatedRoute.getId(),
                    event.getGrowthTracks() != null ? event.getGrowthTracks().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (TalentRouteProcessingException e) {
            log.error("Failed to update talent route for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        } catch (Exception e) {
            log.error("Unexpected error updating talent route for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        }
    }
}