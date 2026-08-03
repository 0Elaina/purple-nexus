package com.purple.health;

import java.time.Duration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.purple.config.ObjectStorageProperties;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * 检查应用配置的 RustFS 存储桶是否可以正常访问。
 *
 * 这里使用 S3 标准的 HeadBucket 请求，只读取存储桶状态，
 * 不会上传、下载、修改或删除任何对象。
 */
@Component("s3")
public class ObjectStorageHealthIndicator implements HealthIndicator {
    private static final Duration CHECK_TIMEOUT = Duration.ofSeconds(3);

    private final S3Client s3Client;
    private final ObjectStorageProperties properties;

    /**
     * Spring 会把已经创建好的 S3Client 和对象存储配置传入这里
     *
     * @param s3Client   访问 RustFS 的 S3 客户端
     * @param properties 包含目标存储桶名称的配置
     */
    public ObjectStorageHealthIndicator(
            S3Client s3Client,
            ObjectStorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /**
     * 返回当前对象存储的健康状态
     *
     * @return 存储桶可访问时返回 UP，否则返回 DOWN
     */
    @Override
    public Health health() {
        HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(properties.bucket())
                .overrideConfiguration(configuration -> configuration.apiCallTimeout(CHECK_TIMEOUT))
                .build();

        try {
            s3Client.headBucket(request);
            return Health.up().build();
        } catch (SdkException exception) {
            return Health.down().build();
        }
    }
}
