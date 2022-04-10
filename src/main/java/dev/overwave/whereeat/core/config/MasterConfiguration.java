package dev.overwave.whereeat.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(PropertiesConfiguration.class)
@EnableScheduling
public class MasterConfiguration {
}

