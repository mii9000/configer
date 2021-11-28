package com.mii9000.configer;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for simple App.
 */
public class ConfigerClientDebug
{

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    //@Test
    public void SQSConsumerIntegrationDebugger() throws JMSException {
        SQSConsumer consumer = new SQSConsumer("test","s3_events","ObjectCreated:Put");

        IFileReader s3FileReader = new S3FileReader("test", "configer");

        IFileModifier fileModifier = new FileModifier(new HashSet<>(Collections.singletonList("config.properties")), s3FileReader);

        consumer.Subscribe(fileModifier);
    }

    //@Test
    public void S3ReaderIntegrationDebugger() throws IOException {
        IFileReader s3Reader = new S3FileReader("test", "configer");
        String contents = s3Reader.Read("config.properties");
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

}
