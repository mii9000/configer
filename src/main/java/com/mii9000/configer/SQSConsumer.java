package com.mii9000.configer;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.io.IOException;
import java.util.List;

public class SQSConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQSConsumer.class);
    private final SqsClient _client;
    private final String _queueUrl;

    public SQSConsumer(String CredentialProfile, String QueueUrl) {
        _queueUrl = QueueUrl;
        _client = SqsClient
                .builder()
                .credentialsProvider(ProfileCredentialsProvider.create(CredentialProfile))
                .build();
    }

    public void Subscribe(IFileModifier fileModifier) {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                        .queueUrl(_queueUrl)
                        .maxNumberOfMessages(1)
                        .build();

            LOGGER.info("Listening for messages...");

            while (true) {
                List<Message> messages = _client.receiveMessage(receiveMessageRequest).messages();

                for (Message message : messages) {
                    String body = message.body();
                    fileModifier.Modify(body);
                    //deleteMessage(queueUrl, message);
                }
            }
        } catch(SqsException | IOException | ConfigurationException e) {
            //LOGGER.error(e.awsErrorDetails().errorMessage());
        } finally {
            _client.close();
        }
    }

    /**
     * Message from queue needs to be deleted once processing done
     * @param queueUrl SQS Url
     * @param message Message handler
     */
    private void deleteMessage(String queueUrl, Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        _client.deleteMessage(deleteMessageRequest);
        LOGGER.info("Deleted message : " + message.messageId());
    }
}
