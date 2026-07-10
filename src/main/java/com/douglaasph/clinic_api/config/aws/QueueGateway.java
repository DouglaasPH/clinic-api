package com.douglaasph.clinic_api.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class QueueGateway {
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public QueueGateway(SqsClient sqsClient,
                        ObjectMapper objectMapper,
                        @Value("${aws.sqs.queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public void sendExamNotification(Long reportId, String s3Key) {
        try {
            Map<String, Object> payload = Map.of(
                    "reportId", reportId,
                    "s3Key", s3Key
            );
            String messageBody = objectMapper.writeValueAsString(payload);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMsgRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to the SQS queue", e);
        }
    }
}
