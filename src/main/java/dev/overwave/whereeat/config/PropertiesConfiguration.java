package dev.overwave.whereeat.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(value = "whereeat", ignoreUnknownFields = false)
public class PropertiesConfiguration {

    private Login login;
    private Client client;
    private Path path;

    @Data
    public static class Login {
        private long phone;
        private int apiId;
        private String apiHash;
    }

    @Data
    public static class Client {
        /**
         * Not used yet.
         */
        private String databaseKey;

        /**
         * Chat title in Unicode encoding.
         */
        private String chatName;
    }

    @Data
    public static class Path {
        /**
         * Not used yet.
         */
        private String database;
        /**
         * Not used yet.
         */
        private String downloads;
    }
}