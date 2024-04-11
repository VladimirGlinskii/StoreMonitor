package ru.vglinskii.storemonitor.decommissionedreportsimulator.api;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(StorageApi.class);
    private AmazonS3 storageClient;
    private String bucketName;

    public StorageApi(AWSCredentials credentials, String bucketName) {
        this.storageClient = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                "storage.yandexcloud.net",
                                "ru-central1"
                        )
                )
                .build();
        this.bucketName = bucketName;
    }

    public void uploadObject(String key, byte[] content) {
        try (var input = new ByteArrayInputStream(content)) {
            PutObjectRequest request = new PutObjectRequest(
                    bucketName,
                    key,
                    input,
                    new ObjectMetadata()
            );
            storageClient.putObject(request);
        } catch (IOException | SdkClientException e) {
            LOGGER.error("Failed to upload object with key = {}", key, e);
            throw new StorageException(e);
        }
    }
}
