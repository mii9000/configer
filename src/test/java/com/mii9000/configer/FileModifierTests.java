package com.mii9000.configer;

import static org.junit.Assert.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FileModifierTests {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                    "Upsert 1",
                    String.format("env=prod%skey=007", System.lineSeparator()),
                    "config.properties",
                    String.format("env=prod%skey=007", System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "Upsert 2",
                    String.format("env=test%skey=123", System.lineSeparator()),
                    "config.properties",
                    String.format("env=test%skey=123", System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "Upsert 3",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "Event Not Matched",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    _defaultConfigProperties,
                    "event2.json",
                    "ObjectCreated:Put"
                },
                {
                    "File Not Matched",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    _defaultConfigProperties,
                    "event3.json",
                    "ObjectCreated:Put"
                },
                {
                    "Do Not Delete Configs 1",
                    "",
                    "config.properties",
                    _defaultConfigProperties,
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "Do Not Delete Configs 2",
                    "uuu=000",
                    "config.properties",
                    _defaultConfigProperties,
                    "event1.json",
                    "ObjectCreated:Put"
                }
        });
    }

    private String _message;
    private String _sourceContent;
    private String _destinationFileName;
    private String _destinationContent;
    private String _eventFileName;
    private String _targetEvent;
    private final static String _defaultConfigProperties = String.format("env=test%skey=007", System.lineSeparator());

    public FileModifierTests(String message,
                             String sourceContent,
                             String destinationFileName,
                             String destinationContent,
                             String eventFileName,
                             String targetEvent) {
        _message = message;
        _sourceContent = sourceContent;
        _destinationFileName = destinationFileName;
        _destinationContent = destinationContent;
        _eventFileName = eventFileName;
        _targetEvent = targetEvent;
    }

    @Test
    public void FileModifier_Modify_Test() throws IOException {
        //RESET
        WriteToFile(_destinationFileName, _defaultConfigProperties);

        //ARRANGE
        IFileReader fileReader = new MockFileReader(_sourceContent);
        HashSet<String> files = new HashSet<>();
        files.add(_destinationFileName);
        IFileModifier fileModifier = new FileModifier(files, fileReader);
        String eventJson = ReadFromFile(_eventFileName);

        //ACT
        fileModifier.Modify(eventJson, _targetEvent);

        //ASSERT
        String configFileContent = ReadFromFile(_destinationFileName);
        assertEquals(_message, _destinationContent, configFileContent);
    }

    private String ReadFromFile(String fileName) throws IOException {
        String eventFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).getPath();
        return new String(Files.readAllBytes(Paths.get(eventFilePath)));
    }

    private void WriteToFile(String fileName, String content) throws IOException {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(fileName)).getPath();
        FileWriter writer = new FileWriter(filePath, false);
        writer.write(content);
        writer.close();
    }
}
