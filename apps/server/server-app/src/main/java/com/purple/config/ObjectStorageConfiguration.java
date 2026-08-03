package com.purple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * 根据应用配置组装访问 S3 兼容对象存储所需的客户端。
 */
@Configuration(proxyBeanMethods = false)
public class ObjectStorageConfiguration {

    @Bean
    S3Client s3Client(ObjectStorageProperties properties) {
        // 从配置属性中读取 AccessKey 和 SecretKey，构建 AWS 基本凭证对象
        // 这是后续创建 S3 客户端时进行身份认证的必要凭据
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            properties.accessKey(),
            properties.secretKey()
        );

        // 构建 S3 服务配置，开启或关闭路径式访问（path-style access）
        // 对于 RustFS 等非 AWS 的对象存储服务，通常需要启用路径式访问
        // 路径式：https://host/bucket/key；虚拟主机式：https://bucket.host/key
        S3Configuration serviceConfiguration = S3Configuration.builder()
            .pathStyleAccessEnabled(properties.pathStyleAccess())
            .build();
        
        // 组装 S3 客户端：配置自定义终端地址、静态凭证、区域和服务配置
        // endpointOverride 用于指向 RustFS 等非 AWS 的对象存储服务地址
        // credentialsProvider 用于提供静态凭证，无需动态获取
        // region 用于指定 S3 服务的区域，如 "us-east-1"
        // serviceConfiguration 用于配置 S3 服务的路径式访问（path-style access）
        return S3Client.builder()
            .endpointOverride(properties.endpoint())
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(properties.region()))
            .serviceConfiguration(serviceConfiguration)
            .build();
    }
}
