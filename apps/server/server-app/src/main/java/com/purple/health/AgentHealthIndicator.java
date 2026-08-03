package com.purple.health;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.purple.config.AgentProperties;

/**
 * 通过 FastAPI Agent 的 readiness 端点检查 AI 服务是否可用。
 *
 * 该检查不调用任何 AI 业务接口，只读取 Agent 已经建立的
 * /health/ready 契约。Agent 会在内部检查 Redis 和 Qdrant。
 */
@Component("agent")
public class AgentHealthIndicator implements HealthIndicator {
    // 检查 Agent 健康的超时时间
    private static final Duration CHECK_TIMEOUT = Duration.ofSeconds(3);
    private static final int HTTP_OK = 200;

    private final HttpClient httpClient;
    private final URI readinessUri;

    /**
     * 创建生产运行使用的 Agent 健康检查。
     *
     * HttpClient 在组件创建时只构造一次，后续健康请求会复用它，
     * 不会为每次检查重复创建客户端和连接资源。
     *
     * @param properties Agent 服务地址和服务间配置
     */
    @Autowired
    public AgentHealthIndicator(AgentProperties properties) {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(CHECK_TIMEOUT)
                        .build(),
                properties);
    }

    /**
     * 允许测试传入可控制的 HttpClient。
     *
     * 这个构造方法保持包级可见，只供同包测试使用；
     * Spring 正常运行时使用上面的公开构造方法。
     *
     * @param httpClient 用于发送健康请求的 HTTP 客户端
     * @param properties Agent 服务配置
     */
    AgentHealthIndicator(
            HttpClient httpClient,
            AgentProperties properties) {
        this.httpClient = httpClient;
        this.readinessUri = properties.baseUrl().resolve("/health/ready");
    }

    /**
     * 请求 Agent readiness 并转换为 Spring Actuator 健康状态。
     *
     * @return Agent 返回 HTTP 200 时为 UP，否则为 DOWN
     */
    @Override
    public Health health() {
        HttpRequest request = HttpRequest.newBuilder(readinessUri)
                .timeout(CHECK_TIMEOUT)
                .GET()
                .build();

        try {
            // 发送同步健康检测请求：
            // 使用 discarding() 丢弃响应体，因为探针只需校验 HTTP 200 状态码，
            // 避免高频健康检查时在堆内存分配无用对象，降低 GC 开销。
            HttpResponse<Void> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == HTTP_OK) {
                return Health.up().build();
            }
            // Agent 返回 503 或其他非 200 状态时，
            // Server 只报告 DOWN，不暴露 Agent 内部结构。
            return Health.down().build();
        } catch (IOException exception) {
            // 连接拒绝、网络不可达和请求读取失败都表示当前不可用
            return Health.down().build();
        } catch (InterruptedException exception) {
            // Java 用中断标记通知线程停止工作。
            // 捕获 InterruptedException 后必须恢复标记，
            // 避免上层框架误以为线程没有被中断。
            Thread.currentThread().interrupt();
            return Health.down().build();
        }
    }
}
