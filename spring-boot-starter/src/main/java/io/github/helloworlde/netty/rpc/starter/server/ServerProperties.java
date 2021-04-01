package io.github.helloworlde.netty.rpc.starter.server;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "netty.prc.server")
public class ServerProperties {

    private int port = 9090;

    @Value("${spring.application.name:server}")
    private String name;

    private String address;

    private Map<String, String> metadata = new HashMap<>();
}
