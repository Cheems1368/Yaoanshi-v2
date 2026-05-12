package com.medicine.service.storage;

import com.medicine.config.ObjectStorageProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Service
@Profile("!test")
public class S3CompatibleObjectStorageImpl implements S3CompatibleObjectStorage {

    private static final Logger log = LoggerFactory.getLogger(S3CompatibleObjectStorageImpl.class);

    /** Default presigned URL expiry in seconds (1 hour). */
    private static final int DEFAULT_EXPIRY_SECONDS = 3600;

    private final ObjectStorageProperties props;
    private S3Client s3Client;
    private S3Presigner presigner;

    public S3CompatibleObjectStorageImpl(ObjectStorageProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
        var region = Region.of(props.getRegion() != null ? props.getRegion() : "us-east-1");
        var internalEndpoint = URI.create(props.getEndpoint());
        var publicEndpoint = URI.create(resolvePublicEndpoint());

        this.s3Client = S3Client.builder()
                .endpointOverride(internalEndpoint)
                .credentialsProvider(credentials)
                .region(region)
                .forcePathStyle(true)
                .build();

        this.presigner = S3Presigner.builder()
                .endpointOverride(publicEndpoint)
                .credentialsProvider(credentials)
                .region(region)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();

        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(props.getBucket()).build());
        } catch (NoSuchBucketException e) {
            log.info("Bucket '{}' not found, creating...", props.getBucket());
            s3Client.createBucket(CreateBucketRequest.builder().bucket(props.getBucket()).build());
            log.info("Bucket '{}' created.", props.getBucket());
        } catch (Exception e) {
            log.warn("Could not verify bucket '{}': {}", props.getBucket(), e.getMessage());
        }
    }

    @Override
    public String createPresignedUploadUrl(String objectKey) {
        var putRequest = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .build();
        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(DEFAULT_EXPIRY_SECONDS))
                .putObjectRequest(putRequest)
                .build();
        return presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public String createPresignedDownloadUrl(String objectKey) {
        var getRequest = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .build();
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(DEFAULT_EXPIRY_SECONDS))
                .getObjectRequest(getRequest)
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public byte[] readObjectBytes(String objectKey) {
        var getRequest = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .build();
        return s3Client.getObject(getRequest, ResponseTransformer.toBytes()).asByteArray();
    }

    private String resolvePublicEndpoint() {
        return StringUtils.hasText(props.getPublicEndpoint()) ? props.getPublicEndpoint() : props.getEndpoint();
    }
}
