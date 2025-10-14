package com.capstone.messaging;

import com.capstone.exception.AssessmentProcessingException;
import com.capstone.model.AssessmentSnapshot;
import com.capstone.service.AssessmentSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.AssessmentResultEvent;
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
public class AssessmentKafkaConsumer {

    private final AssessmentSnapshotService assessmentSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_ASSESSMENT_RESULT_DLT_TOPIC:assessment.result.dlt}")
    private String assessmentDltTopic;

    @KafkaListener(topics = "${KAFKA_ASSESSMENT_RESULT_TOPIC:assessment.result.create}")
    @Retryable(
            retryFor = {AssessmentProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenAssessmentResult(
            @Payload AssessmentResultEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received assessment.result event from topic: {}, partition: {}, offset: {}, userId: {}, assessmentId: {}, attempt: {}",
                topic, partition, offset, event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());

        try {
            // Validate event - MOVED FROM SERVICE
            validateAssessmentEvent(event);

            // Process the assessment event
            AssessmentSnapshot processedAssessment = assessmentSnapshotService.processAssessmentEvent(event);

            log.info("Successfully processed assessment event for userId: {}, assessmentId: {}, attempt: {}, internal ID: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), processedAssessment.getId());

            acknowledgment.acknowledge();

        } catch (AssessmentProcessingException e) {
            log.error("Failed to process assessment event for userId: {}, assessmentId: {}, attempt: {}. Error: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing assessment event for userId: {}, assessmentId: {}, attempt: {}. Error: {}",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateAssessmentEvent(AssessmentResultEvent event) {
        log.debug("Validating assessment event: {}", event);

        if (event == null) {
            throw new AssessmentProcessingException("Assessment event cannot be null");
        }

        if (event.getUserId() == null) {
            throw new AssessmentProcessingException("User ID is required");
        }

        if (event.getAssessmentId() == null) {
            throw new AssessmentProcessingException("Assessment ID is required");
        }

        if (event.getAssessmentName() == null || event.getAssessmentName().trim().isEmpty()) {
            throw new AssessmentProcessingException("Assessment name is required");
        }

        if (event.getMoodleQuizId() <= 0) {
            throw new AssessmentProcessingException("Valid Moodle Quiz ID is required");
        }

        if (event.getGrade() < 0 || event.getGrade() > 100) {
            throw new AssessmentProcessingException("Grade must be between 0 and 100, got: " + event.getGrade());
        }

        if (event.getTimeStart() == null) {
            throw new AssessmentProcessingException("Start time is required");
        }

        if (event.getTimeFinish() == null) {
            throw new AssessmentProcessingException("Finish time is required");
        }

        if (event.getTimeFinish().isBefore(event.getTimeStart())) {
            throw new AssessmentProcessingException("Finish time cannot be before start time");
        }

        if (event.getAttemptNumber() < 1) {
            throw new AssessmentProcessingException("Attempt number must be positive, got: " + event.getAttemptNumber());
        }

        // NOW AVAILABLE - validate capsuleId
        if (event.getCapsuleId() == null) {
            throw new AssessmentProcessingException("Capsule ID is required");
        }

        log.debug("Assessment event validation successful for userId: {}, assessmentId: {}, attempt: {}",
                event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());
    }

    private void handleProcessingFailure(AssessmentResultEvent event, Acknowledgment acknowledgment) {
        String eventKey = event.getUserId() + "-" + event.getAssessmentId() + "-" + event.getAttemptNumber();

        log.error("Processing failed for assessment event with userId: {}, assessmentId: {}, attempt: {}. Sending to DLT topic: {}",
                event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), assessmentDltTopic);

        try {
            kafkaTemplate.send(assessmentDltTopic, eventKey, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed assessment event to DLT for userId: {}, assessmentId: {}, attempt: {}",
                                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber());
                        } else {
                            log.error("Failed to send assessment event to DLT for userId: {}, assessmentId: {}, attempt: {}",
                                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), ex);
                        }
                    });

            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send assessment message to DLT for userId: {}, assessmentId: {}, attempt: {}. " +
                            "Message will be retried by Kafka",
                    event.getUserId(), event.getAssessmentId(), event.getAttemptNumber(), dltException);
        }
    }
}
