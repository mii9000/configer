package com.mii9000.configer;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for simple App.
 */
public class SQSConsumerTest
{

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void TestIntegrationRun() {
        //SQSConsumer sut = new SQSConsumer("test", Collections.emptyList());
        //sut.Subscribe("https://sqs.ap-south-1.amazonaws.com/444151335418/s3_events");

        /*
        * var s3Config = new S3Config(profile, bucket, file);
        * var s3Reader = new S3FileReader(s3Config);
        *
        * var fileModifier = new FileModifier(HashSet<String> listOfFilesToWatchForUpdate, s3Reader);
        *
        * var consumerConfig = new SQSConsumerConfig(profile, url);
        * var consumer = new SQSConsumer(consumerConfig);
        * consumer.Subscribe(fileModifier);
        * */
    }

    @Test
    public void S3ReaderIntegrationTest() {
        IS3FileReader s3Reader = new S3FileReader("test", "configer");
        try {
            String contents = s3Reader.Read("config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void FileModifierIntegrationTest() {
        //s3 reader
        IS3FileReader s3Reader = new S3FileReader("test", "configer");

        //file modifier
        HashSet<String> files = new HashSet<>();
        files.add("config.properties");
        IFileModifier fileModifier = new FileModifier(files, s3Reader);

        try {
            String path = this.getClass().getClassLoader().getResource("event.json").getPath();
            String eventJson = new String(Files.readAllBytes(Paths.get(path)));

            //execute
            fileModifier.Modify(eventJson);
        } catch (IOException | ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAppMain()
    {
        //App.main(null);
        try {
            assertEquals("Hello World!" + System.getProperty("line.separator"), outContent.toString());
        } catch (AssertionError e) {
            fail("\"message\" is not \"Hello World!\"");
        }
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

}
