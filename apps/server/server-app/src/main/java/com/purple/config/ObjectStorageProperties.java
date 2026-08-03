package com.purple.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 描述 S3 兼容对象存储的连接配置
 *
 * @param endpoint        S3 API 地址
 * @param accessKey       访问标识
 * @param secretKey       访问密钥
 * @param bucket          默认存储桶
 * @param region          请求签名区域
 * @param pathStyleAccess 是否启用 Path Style 访问
 */
@Validated
@ConfigurationProperties(prefix = "s3")
public record ObjectStorageProperties(
        @NotNull URI endpoint,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String bucket,
        @NotBlank String region,
        @NotNull Boolean pathStyleAccess) {
}
