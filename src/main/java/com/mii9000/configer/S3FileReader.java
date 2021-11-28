package com.mii9000.configer;

import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class S3FileReader implements IFileReader {
    private final S3Client _client;
    private final String _bucket;

    public S3FileReader(String CredentialProfile, String Bucket) {
        _bucket = Bucket;
        _client = S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create(CredentialProfile))
                .build();
    }

    /**
     * File Reader wrapper over AWS S3 SDK
     * @param FileName Name of file in bucket
     * @return
     * @throws IOException
     */
    public String Read(String FileName) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(_bucket)
                .key(FileName)
                .build();

        ResponseInputStream<GetObjectResponse> objectResponse = _client.getObject(getObjectRequest);
        return IOUtils.toString(objectResponse, StandardCharsets.UTF_8);
    }
}
