package com.capstone.messaging;

import com.capstone.exception.ClusterProcessingException;
import com.capstone.model.ClusterSnapshot;
import com.capstone.service.ClusterSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ClusterEvent;
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
public class ClusterKafkaConsumer {
    private final ClusterSnapshotService clusterSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_CLUSTER_DLT_TOPIC:cluster.create.dlt}")
    private String clusterDltTopic;

    @KafkaListener(topics = "${KAFKA_CLUSTER_TOPIC:cluster.create}")
    @Retryable(
            retryFor = {ClusterProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenClusterCreate(
            @Payload ClusterEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received cluster.create event from topic: {}, partition: {}, offset: {}, clusterId: {}",
                topic, partition, offset, event.getClusterId());

        try {
            // Validate event
            validateClusterEvent(event);

            // Process the cluster event
            ClusterSnapshot processedCluster = clusterSnapshotService.processClusterEvent(event);

            log.info("Successfully processed cluster event for ClusterId: {}, internal ID: {}",
                    event.getClusterId(), processedCluster.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (ClusterProcessingException e) {
            log.error("Failed to process cluster event for ClusterId: {}. Error: {}",
                    event.getClusterId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing cluster event for ClusterId: {}. Error: {}",
                    event.getClusterId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateClusterEvent(ClusterEvent event) {
        log.debug("Validating cluster event: {}", event);

        if (event == null) {
            throw new ClusterProcessingException("Cluster event cannot be null");
        }

        if (event.getClusterId() == null) {
            throw new ClusterProcessingException("Cluster event must contain a valid ID");
        }

        if (event.getClusterName() == null || event.getClusterName().trim().isEmpty()) {
            throw new ClusterProcessingException("Cluster event must contain a valid name");
        }

        log.debug("Cluster event validation successful for clusterId: {}", event.getClusterId());
    }

    private void handleProcessingFailure(ClusterEvent event, Acknowledgment acknowledgment) {
        String clusterId = event.getClusterId().toString();

        log.error("Processing failed for cluster event with clusterId: {}. Sending to DLT topic: {}",
                clusterId, clusterDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(clusterDltTopic, clusterId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed cluster event to DLT for clusterId: {}", clusterId);
                        } else {
                            log.error("Failed to send cluster event to DLT for clusterId: {}", clusterId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send cluster message to DLT for clusterId: {}. Message will be retried by Kafka",
                    clusterId, dltException);
        }
    }

    @KafkaListener(topics = "${KAFKA_CLUSTER_UPDATE_TOPIC:cluster.update}")
    @Retryable(
            retryFor = {ClusterProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenClusterUpdate(
            @Payload ClusterEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received cluster.update event from topic: {}, partition: {}, offset: {}, clusterId: {}",
                topic, partition, offset, event.getClusterId());

        try {
            // Validate event
            validateClusterEvent(event);

            // Process the cluster update using existing service logic
            ClusterSnapshot updatedCluster = clusterSnapshotService.processClusterEvent(event);

            log.info("Successfully updated cluster for clusterId: {}, internal ID: {}",
                    event.getClusterId(), updatedCluster.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (ClusterProcessingException e) {
            log.error("Failed to update cluster for clusterId: {}. Error: {}",
                    event.getClusterId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error updating cluster for clusterId: {}. Error: {}",
                    event.getClusterId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }
}

