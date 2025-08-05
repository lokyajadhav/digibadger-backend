package com.taashee.badger.health;

import com.taashee.badger.models.OrganizationApiConfig;
import com.taashee.badger.repositories.OrganizationApiConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@lombok.extern.slf4j.Slf4j
public class ApiIntegrationHealthIndicator implements org.springframework.boot.actuator.health.HealthIndicator {

    @org.springframework.beans.factory.annotation.Autowired
    private com.taashee.badger.repositories.OrganizationApiConfigRepository apiConfigRepository;

    @Override
    public org.springframework.boot.actuator.health.Health health() {
        try {
            java.util.List<com.taashee.badger.models.OrganizationApiConfig> configs = apiConfigRepository.findByIsActiveTrue();
            
            if (configs.isEmpty()) {
                return org.springframework.boot.actuator.health.Health.up()
                    .withDetail("message", "No active API integrations configured")
                    .withDetail("total_configs", 0)
                    .withDetail("healthy_configs", 0)
                    .withDetail("unhealthy_configs", 0)
                    .build();
            }

            long healthyConfigs = configs.stream()
                .filter(com.taashee.badger.models.OrganizationApiConfig::isHealthy)
                .count();
            
            long unhealthyConfigs = configs.size() - healthyConfigs;
            
            java.util.Map<String, Object> details = java.util.Map.of(
                "total_configs", configs.size(),
                "healthy_configs", healthyConfigs,
                "unhealthy_configs", unhealthyConfigs,
                "health_percentage", Math.round((double) healthyConfigs / configs.size() * 100),
                "last_check", java.time.LocalDateTime.now().toString()
            );

            if (unhealthyConfigs == 0) {
                return org.springframework.boot.actuator.health.Health.up()
                    .withDetails(details)
                    .withDetail("message", "All API integrations are healthy")
                    .build();
            } else if (healthyConfigs > 0) {
                return org.springframework.boot.actuator.health.Health.up()
                    .withDetails(details)
                    .withDetail("message", "Some API integrations are unhealthy")
                    .build();
            } else {
                return org.springframework.boot.actuator.health.Health.down()
                    .withDetails(details)
                    .withDetail("message", "All API integrations are unhealthy")
                    .build();
            }
        } catch (Exception e) {
            log.error("Error checking API integration health", e);
            return org.springframework.boot.actuator.health.Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("last_check", java.time.LocalDateTime.now().toString())
                .build();
        }
    }
} 