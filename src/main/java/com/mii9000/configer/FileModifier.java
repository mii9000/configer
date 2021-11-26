package com.mii9000.configer;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class FileModifier implements IFileModifier {

    private final HashSet<String> _listOfFilesToWatchForUpdate;
    private final IS3FileReader _is3FileReader;
    private final Gson _gson;

    public FileModifier(HashSet<String> listOfFilesToWatchForUpdate, IS3FileReader is3FileReader) {
        _listOfFilesToWatchForUpdate = listOfFilesToWatchForUpdate;
        _is3FileReader = is3FileReader;
        _gson = new GsonBuilder().create();
    }

    /**
     * Entry method into logic of this class
     * @param MessageEventBody S3 event published from SQS which comes as String
     * @throws IOException
     * @throws ConfigurationException
     */
    @Override
    public void Modify(String MessageEventBody) throws IOException, ConfigurationException {
        EventMessageBody messageBody = _gson.fromJson(MessageEventBody, EventMessageBody.class);
        S3EventNotification eventNotification = S3EventNotification.parseJson(messageBody.Message);
        List<S3EventNotification.S3EventNotificationRecord> records = eventNotification.getRecords();
        for (S3EventNotification.S3EventNotificationRecord record : records) RecordHandler(record);
    }

    /**
     * Main logic to handle each event from S3
     * @param record S3 Record inside S3 notification
     * @throws IOException
     * @throws ConfigurationException
     */
    private void RecordHandler(S3EventNotification.S3EventNotificationRecord record) throws IOException, ConfigurationException {
        String eventName = record.getEventName();
        if(Objects.equals(eventName, "ObjectCreated:Put")) {
            S3EventNotification.S3Entity s3Entity = record.getS3();
            S3EventNotification.S3ObjectEntity s3Object = s3Entity.getObject();
            String key = s3Object.getKey();
            if (_listOfFilesToWatchForUpdate.contains(key)) {
                String fileContents = _is3FileReader.Read(key);
                ReplaceFileConfig(key, fileContents);
            }
        }
    }

    /**
     * Overwrites the contents of the lcoal file with contents from S3
     * @param FileName Name of local config file
     * @param FileContents Contents of S3 config file
     * @throws IOException
     */
    private void ReplaceFileConfig(String FileName, String FileContents) throws IOException {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(FileName)).getPath();
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            FileWriter writer = new FileWriter(filePath, false);
            writer.write(FileContents);
            writer.close();
        }
    }

    /**
     * Updates local config files by each config key
     * @param FileName Name of the config file
     * @param FileContents Contents of the file from S3
     * @throws IOException
     * @throws ConfigurationException
     */
    private void UpdateFileConfig(String FileName, String FileContents) throws IOException, ConfigurationException {
        final Properties properties = new Properties();
        properties.load(new StringReader(FileContents));

        Configurations configs = new Configurations();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(FileName)).getPath();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(configPath);
        PropertiesConfiguration config = builder.getConfiguration();
        properties.forEach((key, value) -> {
            String sKey = key.toString();
            if (config.containsKey(sKey)) {
                config.setProperty(sKey, value);
            } else {
                config.addProperty(sKey, value);
            }
        });

        builder.save();
    }
}
