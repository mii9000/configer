package com.mii9000.configer;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.message.SQSTextMessage;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.io.IOException;

public class SQSConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQSConsumer.class);

    private final String _watchEvent;
    private final String _queue;
    private final SQSConnectionFactory _connectionFactory;

    public SQSConsumer(String CredentialProfile, String Queue, String WatchEvent) {
        _queue = Queue;
        _watchEvent = WatchEvent;
        _connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withCredentials(new com.amazonaws.auth.profile.ProfileCredentialsProvider(CredentialProfile))
        );
    }

    public void Subscribe(IFileModifier fileModifier) throws JMSException {
        SQSConnection connection = _connectionFactory.createConnection();

        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        MessageConsumer consumer = session.createConsumer(session.createQueue( _queue));

        connection.start();

        receiveMessages(consumer, fileModifier, _watchEvent);

        connection.close();

        LOGGER.debug("Connection closed");
    }

    private static void receiveMessages(MessageConsumer consumer, IFileModifier fileModifier, String watchEvent) {
        try {
            while(true) {
                LOGGER.debug("Listening for config messages...");

                javax.jms.Message message = consumer.receive();

                LOGGER.info("Received Message ID : " + message.getJMSMessageID());

                SQSTextMessage sqsTextMessage = (SQSTextMessage)message;

                String eventText = sqsTextMessage.getText();

                LOGGER.debug(eventText);

                fileModifier.Modify(eventText, watchEvent);

                LOGGER.debug("Finished File Modification");

                sqsTextMessage.acknowledge();

                LOGGER.debug("Message Acknowledged : " + message.getJMSMessageID());
            }
        } catch (ConfigurationException | IOException | JMSException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
