package com.mii9000.configer;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class FileModifier implements IFileModifier {

    private final HashSet<String> _listOfFilesToWatchForUpdate;
    private final IFileReader _iFileReader;
    private final Gson _gson;

    public FileModifier(HashSet<String> listOfFilesToWatchForUpdate, IFileReader iFileReader) {
        _listOfFilesToWatchForUpdate = listOfFilesToWatchForUpdate;
        _iFileReader = iFileReader;
        _gson = new GsonBuilder().create();
    }

    /**
     * Entry method into logic of this class
     * @param MessageEventBody S3 event published from SQS which comes as String
     * @throws IOException
     */
    @Override
    public void Modify(String MessageEventBody, String WatchEvent) throws IOException, ConfigurationException {
        EventMessageBody messageBody = _gson.fromJson(MessageEventBody, EventMessageBody.class);
        S3EventNotification eventNotification = S3EventNotification.parseJson(messageBody.Message);
        List<S3EventNotification.S3EventNotificationRecord> records = eventNotification.getRecords();
        for (S3EventNotification.S3EventNotificationRecord record : records) {
            //only process record if event is of concern
            if(Objects.equals(record.getEventName(), WatchEvent)) RecordHandler(record);
        }
    }

    /**
     * Main logic to handle each event from S3
     * @param record S3 Record inside S3 notification
     * @throws IOException
     */
    private void RecordHandler(S3EventNotification.S3EventNotificationRecord record) throws IOException, ConfigurationException {
        S3EventNotification.S3Entity s3Entity = record.getS3();
        S3EventNotification.S3ObjectEntity s3Object = s3Entity.getObject();
        String filename = s3Object.getKey();
        if (_listOfFilesToWatchForUpdate.contains(filename)) {
            String fileContents = _iFileReader.Read(filename);
            UpsertConfig(filename, fileContents);
        }
    }

    /**
     * Upsert local config files by each config key
     * @param FileName Name of the config file
     * @param FileContents Contents of the file from S3
     * @throws IOException
     * @throws ConfigurationException
     */
    private void UpsertConfig(String FileName, String FileContents) throws IOException, ConfigurationException {
        //remote file contents into Properties
        final Properties properties = new Properties();
        properties.load(new StringReader(FileContents));

        //read configuration from local file
        Configurations configs = new Configurations();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader().getResource(FileName)).getPath();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(configPath);
        PropertiesConfiguration config = builder.getConfiguration();

        //update matched properties
        boolean doSave = false;
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (config.containsKey(key)) {
                config.setProperty(key, value);
                doSave = true;
            }
        }

        //if there were changes made to file only then save changes
        if(doSave) builder.save();
    }
}
