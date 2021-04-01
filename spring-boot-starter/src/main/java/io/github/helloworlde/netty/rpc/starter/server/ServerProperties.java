package io.github.helloworlde.netty.rpc.starter.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "netty.rpc.server")
public class ServerProperties {

    private boolean enabled = true;

    private int port = 9090;

    @NestedConfigurationProperty
    private Registry registry = new Registry();

    @Data
    static class Registry {

        private String name;

        private String address;

        private Map<String, String> metadata = new HashMap<>();
    }
}
