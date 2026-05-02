package com.vkr.sensor_simulator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SimulatorConfig {

    @Bean
    public RestClient restClient(
            @Value("${simulator.server-url:http://localhost:8080/api/v1/sensor/readings}") String serverUrl) {
        return RestClient.builder()
                .baseUrl(serverUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
