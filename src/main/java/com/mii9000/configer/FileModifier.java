package com.mii9000.configer;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class FileModifier implements IFileModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileModifier.class);

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
    public void Modify(String MessageEventBody, String WatchEvent) throws IOException {
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
    private void RecordHandler(S3EventNotification.S3EventNotificationRecord record) throws IOException {
        S3EventNotification.S3Entity s3Entity = record.getS3();
        S3EventNotification.S3ObjectEntity s3Object = s3Entity.getObject();
        String filename = s3Object.getKey();
        if (_listOfFilesToWatchForUpdate.contains(filename)) {
            String fileContents = _iFileReader.Read(filename);
            ReplaceFileConfig(filename, fileContents);
        }
    }

    /**
     * Overwrites the contents of the local file with contents from S3
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
}
