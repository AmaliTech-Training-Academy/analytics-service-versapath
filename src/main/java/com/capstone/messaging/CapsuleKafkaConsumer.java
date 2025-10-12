package com.capstone.messaging;

import com.capstone.exception.CapsuleProcessingException;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.service.CapsuleSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
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

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapsuleKafkaConsumer {
    private final CapsuleSnapshotService capsuleSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_CAPSULE_DLT_TOPIC:capsule.create.dlt}")
    private String capsuleDltTopic;

    @KafkaListener(topics = "${KAFKA_CAPSULE_TOPIC:capsule.create}")
    @Retryable(
            retryFor = {CapsuleProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleCreate(
            @Payload SkillCapsuleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.create event from topic: {}, partition: {}, offset: {}, capsuleId: {}",
                topic, partition, offset, event.getId());

        try {

            validateCapsuleEvent(event);

            // Process the skill capsule event
            CapsuleSnapshot processedCapsule = capsuleSnapshotService.processCapsuleEvent(event);

            log.info("Successfully processed skill capsule event for capsuleId: {}, internal ID: {}, atoms: {}",
                    event.getId(), processedCapsule.getId(),
                    event.getSkillAtom() != null ? event.getSkillAtom().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (CapsuleProcessingException e) {
            log.error("Failed to process skill capsule event for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");

        } catch (Exception e) {
            log.error("Unexpected error processing skill capsule event for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");
        }
    }

    /**
     * Comprehensive validation of SkillCapsuleEvent
     */
    private void validateCapsuleEvent(SkillCapsuleEvent event) {
        log.debug("Validating skill capsule event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new CapsuleProcessingException("Capsule event cannot be null");
        }

        if (event.getId() == null) {
            throw new CapsuleProcessingException("Capsule event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new CapsuleProcessingException("Capsule event must contain a valid name");
        }

        if (event.getDifficulty() != null && event.getDifficulty().trim().isEmpty()) {
            throw new CapsuleProcessingException("Difficulty level cannot be empty if provided");
        }

        if (event.getProficiencyLevel() != null && event.getProficiencyLevel().trim().isEmpty()) {
            throw new CapsuleProcessingException("Proficiency level cannot be empty if provided");
        }

        log.debug("Skill capsule event validation successful for capsuleId: {}", event.getId());
    }

    /**
     * Handle processing failures with DLT support
     */
    private void handleProcessingFailure(SkillCapsuleEvent event, Acknowledgment acknowledgment, String operationType) {
        String capsuleId = event.getId().toString();

        log.error("Processing failed for skill capsule {} operation with capsuleId: {}. Sending to DLT topic: {}",
                operationType, capsuleId, capsuleDltTopic);

        try {
            // Create enhanced event with operation type for DLT analysis
            Map<String, Object> dltPayload = Map.of(
                    "originalEvent", event,
                    "operationType", operationType,
                    "failureTimestamp", System.currentTimeMillis(),
                    "capsuleId", capsuleId
            );

            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(capsuleDltTopic, capsuleId, dltPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed skill capsule {} event to DLT for capsuleId: {}",
                                    operationType, capsuleId);
                        } else {
                            log.error("Failed to send skill capsule {} event to DLT for capsuleId: {}",
                                    operationType, capsuleId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send skill capsule {} message to DLT for capsuleId: {}. Message will be retried by Kafka",
                    operationType, capsuleId, dltException);
        }
    }

    @KafkaListener(topics = "${KAFKA_CAPSULE_UPDATE_TOPIC:capsule.update}")
    @Retryable(
            retryFor = {CapsuleProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleUpdate(
            @Payload SkillCapsuleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.update event from topic: {}, partition: {}, offset: {}, capsuleId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateCapsuleEvent(event);

            // Process the skill capsule update using existing service logic
            CapsuleSnapshot updatedCapsule = capsuleSnapshotService.processCapsuleEvent(event);

            log.info("Successfully updated skill capsule for capsuleId: {}, internal ID: {}, atoms: {}",
                    event.getId(), updatedCapsule.getId(),
                    event.getSkillAtom() != null ? event.getSkillAtom().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (CapsuleProcessingException e) {
            log.error("Failed to update skill capsule for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");

        } catch (Exception e) {
            log.error("Unexpected error updating skill capsule for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        }
    }
}
