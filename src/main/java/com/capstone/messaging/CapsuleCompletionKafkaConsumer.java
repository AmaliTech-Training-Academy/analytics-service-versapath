package com.capstone.messaging;

import com.capstone.exception.CapsuleCompletionProcessingException;
import com.capstone.model.CompleteCapsule;
import com.capstone.service.CompleteCapsuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.CapsuleCompletionEvent;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class CapsuleCompletionKafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CompleteCapsuleService completeCapsuleService;

    @Value("${KAFKA_CAPSULE_COMPLETION_DLT_TOPIC:capsule.completion.dlt}")
    private String completionDltTopic;

    @KafkaListener(topics = "${KAFKA_CAPSULE_COMPLETION_TOPIC:capsule.completion}")
    @Retryable(
            retryFor = {CapsuleCompletionProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleCompletion(
            @Payload CapsuleCompletionEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.completion event from topic: {}, partition: {}, offset: {}, learnerId: {}, capsuleId: {}",
                topic, partition, offset, event.getLearnerId(), event.getCapsuleId());

        try {
            // Validate event
            validateCapsuleCompletionEvent(event);

            // Process the completion event
            CompleteCapsule capsule = completeCapsuleService.processCapsuleCompletionEvent(event);

            log.info("Successfully processed capsule completion event for learnerId: {}, capsuleId: {}",
                    capsule.getUserSnapshot().getUserId(), capsule.getCapsuleSnapshot().getCapsuleId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (CapsuleCompletionProcessingException e) {
            log.error("Failed to process capsule completion event for learnerId: {}, capsuleId: {}. Error: {}",
                    event.getLearnerId(), event.getCapsuleId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing capsule completion event for learnerId: {}, capsuleId: {}. Error: {}",
                    event.getLearnerId(), event.getCapsuleId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateCapsuleCompletionEvent(CapsuleCompletionEvent event) {
        log.debug("Validating capsule completion event: {}", event);

        if (event == null) {
            throw new CapsuleCompletionProcessingException("Capsule completion event cannot be null");
        }

        if (event.getLearnerId() == null) {
            throw new CapsuleCompletionProcessingException("Learner ID is required");
        }

        if (event.getCapsuleId() == null) {
            throw new CapsuleCompletionProcessingException("Capsule ID is required");
        }

        if (event.getCapsuleName() == null || event.getCapsuleName().trim().isEmpty()) {
            throw new CapsuleCompletionProcessingException("Capsule name is required");
        }

        log.debug("Capsule completion event validation successful for learnerId: {}, capsuleId: {}",
                event.getLearnerId(), event.getCapsuleId());
    }

    private void handleProcessingFailure(CapsuleCompletionEvent event, Acknowledgment acknowledgment) {
        String eventKey = event.getLearnerId() + "-" + event.getCapsuleId();

        log.error("Processing failed for capsule completion event with learnerId: {}, capsuleId: {}. Sending to DLT topic: {}",
                event.getLearnerId(), event.getCapsuleId(), completionDltTopic);

        try {
            // Send to Dead Letter Topic
            kafkaTemplate.send(completionDltTopic, eventKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed capsule completion event to DLT for learnerId: {}, capsuleId: {}",
                                    event.getLearnerId(), event.getCapsuleId());
                        } else {
                            log.error("Failed to send capsule completion event to DLT for learnerId: {}, capsuleId: {}",
                                    event.getLearnerId(), event.getCapsuleId(), ex);
                        }
                    });

            // Acknowledge the original message
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send capsule completion message to DLT for learnerId: {}, capsuleId: {}. Message will be retried by Kafka",
                    event.getLearnerId(), event.getCapsuleId(), dltException);
        }
    }
}

