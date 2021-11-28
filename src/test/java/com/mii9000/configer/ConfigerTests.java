package com.mii9000.configer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for simple App.
 */
public class ConfigerTests
{

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void SQSConsumerIntegrationDebugger() {
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

    //@Test
    public void S3ReaderIntegrationDebugger() throws IOException {
        IFileReader s3Reader = new S3FileReader("test", "configer");
        String contents = s3Reader.Read("config.properties");
    }

    private String ReadFromFile(String fileName) throws IOException {
        String eventFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).getPath();
        return new String(Files.readAllBytes(Paths.get(eventFilePath)));
    }


    @Test
    public void testModify() throws IOException {
        //ARRANGE
        final String newFileContent = "world:hello";
        final String eventFileName = "event.json";
        final String configFileName = "config.properties";
        final String eventName = "ObjectCreated:Put";

        {
            IFileReader fileReader = new MockFileReader(newFileContent);
            HashSet<String> files = new HashSet<>();
            files.add(configFileName);
            IFileModifier fileModifier = new FileModifier(files, fileReader);
            String eventJson = ReadFromFile(eventFileName);

            //ACT
            fileModifier.Modify(eventJson, eventName);
        }

        //ASSERT
        {
            String configFileContent = ReadFromFile(configFileName);
            assertEquals("Local config file not modified according to new file content", newFileContent, configFileContent);
        }
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

}
