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

import org.apache.commons.configuration2.ex.ConfigurationException;
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
                    "0 : Update 1",
                    String.format("env=prod%skey=007", System.lineSeparator()),
                    "config.properties",
                    String.format("env=prod%skey=007%s", System.lineSeparator(), System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "1 : Update 2",
                    String.format("env=test%skey=123", System.lineSeparator()),
                    "config.properties",
                    String.format("env=test%skey=123%s", System.lineSeparator(), System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "2 : Update 3",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    String.format("env=dev%skey=888%s", System.lineSeparator(), System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "3 : Event Not Matched",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    _defaultConfigProperties,
                    "event2.json",
                    "ObjectCreated:Put"
                },
                {
                    "4 : File Not Matched",
                    String.format("env=dev%skey=888", System.lineSeparator()),
                    "config.properties",
                    _defaultConfigProperties,
                    "event3.json",
                    "ObjectCreated:Put"
                },
                {
                    "5 : Do Not Delete Configs 1",
                    "",
                    "config.properties",
                    _defaultConfigProperties,
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "6 : Do Not Delete Configs 2",
                    "key=444",
                    "config.properties",
                    String.format("env=test%skey=444%s", System.lineSeparator(), System.lineSeparator()),
                    "event1.json",
                    "ObjectCreated:Put"
                },
                {
                    "7 : Do Not Add Unmatched Config",
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
    public void FileModifier_Modify_Test() throws IOException, ConfigurationException {
        //ARRANGE
        WriteToFile(_destinationFileName, _defaultConfigProperties);
        IFileReader fileReader = new MockFileReader(_sourceContent);
        HashSet<String> files = new HashSet<>();
        files.add(_destinationFileName);
        IFileModifier fileModifier = new FileModifier(files, fileReader);
        String eventJson = ReadFromFile(_eventFileName);

        //ACT
        fileModifier.Modify(eventJson, _targetEvent);

        //ASSERT
        assertEquals(_message, _destinationContent, ReadFromFile(_destinationFileName));
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
