package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.nameresovler.ServerAddress;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "netty.rpc.client")
public class ClientProperties {

    private boolean enabled = true;

    private boolean enableHeartbeat = true;

    private Map<String, ClientConfig> config = new HashMap<>();

    private long timeout = 10_000L;

    @NestedConfigurationProperty
    private Register register = new Register();

    @NestedConfigurationProperty
    private Resolver resolver = new Resolver();

    @Data
    static class ClientConfig {

        private int coreSize = 10;

        private int maxSize = 100;

        private int connectTimeout = 5000;
    }

    @Data
    static class Resolver {

        private List<ServerAddress> addresses;
    }


    @Data
    static class Register {

        private boolean enabled = true;

        private String name;

        private String address;

        private Map<String, String> metadata = new HashMap<>();
    }
}
