package com.purple.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 描述核心服务调用 FastAPI Agent 所需的内部连接配置。
 *
 * @param baseUrl      Agent 服务基地址
 * @param serviceToken 服务间认证凭据
 */
@Validated
@ConfigurationProperties(prefix = "agent")
public record AgentProperties(
        @NotNull URI baseUrl,
        @NotBlank String serviceToken) {
}
