package dev.overwave.whereeat.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PropertiesConfiguration.class)
public class MasterConfiguration {
}

