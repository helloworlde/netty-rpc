package io.github.helloworlde.netty.rpc.starter.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "netty.rpc.client")
public class ClientProperties {

    private boolean enabled = true;

    private Map<String, ClientConfig> config = new HashMap<>();

    @NestedConfigurationProperty
    private Registry registry = new Registry();

    @Data
    static class ClientConfig {

        private int coreSize = 10;

        private int maxSize = 100;

        private int connectTimeout = 5000;
    }

    @Data
    static class Registry {

        private String name;

        private String address;

        private Map<String, String> metadata = new HashMap<>();
    }
}
