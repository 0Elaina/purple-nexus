package com.purple.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import com.purple.config.ObjectStorageProperties;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

/**
 * 验证对象存储健康检测的核心判断逻辑。
 *
 * 这里不启动 Spring，也不连接真正的 RustFS。
 * 测试只关心两件事：
 *
 * 1. S3 客户端成功访问存储桶时，健康状态是否为 UP。
 * 2. S3 客户端访问失败时，健康状态是否为 DOWN。
 *
 * MockitoExtension 负责在测试开始前创建由 @Mock 标记的模拟对象。
 */
@ExtendWith(MockitoExtension.class)
class ObjectStorageHealthIndicatorTests {

    /**
     * 由 Mockito 创建的模拟 S3 客户端。
     *
     * 它不发送真实网络请求。每个测试可以单独规定：
     * 调用 headBucket() 时应该正常返回，还是抛出异常。
     */
    @Mock
    private S3Client s3Client;

    /**
     * 当前真正要测试的对象。
     *
     * 它会接收上面的模拟客户端，因此测试结果不会受到
     * 虚拟机、网络或者 RustFS 是否启动的影响。
     */
    private ObjectStorageHealthIndicator healthIndicator;

    /**
     * 每个测试运行前，重新创建一个健康检测对象。
     *
     * 测试需要提供 ObjectStorageProperties，因为健康检测需要从中
     * 读取存储桶名称。这里的地址和密钥都是测试假数据，不会真正使用。
     */
    @BeforeEach
    void setUp() {
        ObjectStorageProperties properties = new ObjectStorageProperties(
                URI.create("http://localhost:9000"),
                "test-access-key",
                "test-secret-key",
                "us-east-1",
                "test-bucket",
                true);

        healthIndicator = new ObjectStorageHealthIndicator(
                s3Client,
                properties);
    }

    /**
     * 场景：RustFS 存储桶可以正常访问。
     *
     * 预期：headBucket() 成功返回后，健康检测应该报告 UP。
     */
    @Test
    void reportsUpWhenBucketIsAccessible() {
        // Given：规定模拟客户端收到任意 HeadBucketRequest 时正常返回。
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenReturn(HeadBucketResponse.builder().build());

        // When：执行我们真正要测试的健康检测逻辑。
        Status actualStatus = healthIndicator.health().getStatus();

        // Then：确认最终状态为 UP。
        assertThat(actualStatus).isEqualTo(Status.UP);
    }

    /**
     * 场景：RustFS 存储桶无法访问。
     *
     * 这里使用 SdkClientException 代表客户端侧失败，
     * 例如连接被拒绝、网络不可达或者请求超时。
     *
     * 预期：健康检测捕获异常后报告 DOWN，而不是让异常继续抛出。
     */
    @Test
    void reportsDownWhenBucketCannotBeChecked() {
        // Given：规定模拟客户端执行 headBucket() 时抛出连接异常。
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(
                        SdkClientException.create("test connection failure"));

        // When：执行健康检测。异常应该在 health() 内部被处理。
        Status actualStatus = healthIndicator.health().getStatus();

        // Then：确认外部看到的是 DOWN 状态。
        assertThat(actualStatus).isEqualTo(Status.DOWN);
    }
}