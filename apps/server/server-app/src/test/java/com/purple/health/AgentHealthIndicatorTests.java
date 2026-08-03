package com.purple.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import com.purple.config.AgentProperties;

/**
 * 验证 Agent 健康检查对 HTTP 响应状态码的转换规则。
 *
 * 本测试不启动 Spring，也不连接真实 FastAPI Agent。
 * Mockito 创建的 HttpClient 会返回我们指定的响应，
 * 因而测试结果不会受到网络环境影响。
 */
@ExtendWith(MockitoExtension.class)
class AgentHealthIndicatorTests {

    /**
     * 模拟 Java HTTP 客户端。
     *
     * 调用它的 send() 方法时不会真正发送网络请求，
     * 返回结果由测试代码通过 when(...).thenReturn(...) 指定。
     */
    @Mock
    private HttpClient httpClient;

    /**
     * 模拟 Agent 返回的 HTTP 响应。
     *
     * 每个测试只需要改变 statusCode() 的返回值，
     * 就能模拟 Agent 正常或异常的情况。
     */
    @Mock
    private HttpResponse<Void> response;

    private AgentHealthIndicator healthIndicator;

    /**
     * 每个测试开始前创建被测试对象，并规定模拟客户端返回模拟响应。
     */
    @BeforeEach
    void setUp() throws Exception {
        AgentProperties properties = new AgentProperties(
                URI.create("http://localhost:8000"),
                "test-agent-service-token");

        healthIndicator = new AgentHealthIndicator(
                httpClient,
                properties);

        /*
         * AgentHealthIndicator 会使用 BodyHandler<Void> 丢弃响应体。
         * 这里显式写出泛型类型，帮助 Java 正确推断 send() 的返回类型。
         */
        when(httpClient.send(
                any(HttpRequest.class),
                ArgumentMatchers.<HttpResponse.BodyHandler<Void>>any()))
                .thenReturn(response);
    }

    /**
     * Agent readiness 返回 HTTP 200，代表 Agent 当前可以接收请求。
     */
    @Test
    void reportsUpWhenAgentIsReady() {
        // Given：模拟 Agent readiness 返回 HTTP 200。
        when(response.statusCode()).thenReturn(200);

        // When：执行 Server 中的 Agent 健康检查。
        Status actualStatus = healthIndicator.health().getStatus();

        // Then：Server 应当把 Agent 状态报告为 UP。
        assertThat(actualStatus).isEqualTo(Status.UP);
    }

    /**
     * Agent readiness 返回非 200 状态码，代表 Agent 当前不可用。
     */
    @Test
    void reportsDownWhenAgentIsNotReady() {
        // Given：HTTP 503 是 readiness 检查常用的“尚未就绪”状态。
        when(response.statusCode()).thenReturn(503);

        // When：执行健康检查。
        Status actualStatus = healthIndicator.health().getStatus();

        // Then：非 200 响应应被转换为 DOWN。
        assertThat(actualStatus).isEqualTo(Status.DOWN);
    }
}