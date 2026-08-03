package com.purple.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"SPRING_PROFILES_ACTIVE=test",
		"management.endpoint.health.group.readiness.include=readinessState,db,redis"
})
public class ServerHealthEndpointTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @Qualifier("db")
    private ControllableHealthIndicator databaseHealthIndicator;

    @Autowired
    @Qualifier("redis")
    private ControllableHealthIndicator redisHealthIndicator;

    /**
     * 每个测试方法执行前重置健康指示器状态，确保测试独立性。
     */
    @BeforeEach
    void resetHealthIndicators() {
        databaseHealthIndicator.reportUp();
        redisHealthIndicator.reportUp();
    }

    @Test
    void livenessRemainsUpWhenExternalDependenciesAreDown() throws Exception {
        databaseHealthIndicator.reportDown();
        redisHealthIndicator.reportDown();

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readinessReportsUpWhenDependenciesAreAvailable() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void readinessReportsDownWhenDatabaseIsUnavailable() throws Exception {
        databaseHealthIndicator.reportDown();

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void readinessReportsDownWhenRedisIsUnavailable() throws Exception {
        redisHealthIndicator.reportDown();

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class HealthContributorTestConfiguration {
        @Bean(name = "db")
        ControllableHealthIndicator databaseHealthIndicator() {
            return new ControllableHealthIndicator();
        }

        @Bean(name = "redis")
        ControllableHealthIndicator redisHealthIndicator() {
            return new ControllableHealthIndicator();
        }
    }

    /**
     * 测试专用的可控健康贡献器，避免自动化测试连接真实基础设施。
     */
    static class ControllableHealthIndicator implements HealthIndicator {
        private boolean available = true;

        void reportUp() {
            available = true;
        }

        void reportDown() {
            available = false;
        }

        @Override
        public Health health() {
            return available ? Health.up().build() : Health.down().build();
        }
    }
}
