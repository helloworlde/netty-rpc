package io.github.helloworlde.netty.rpc.registry;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ServiceInfo {

    private String id;

    private String name;

    private String address;

    private Integer port;

    private Map<String, String> metadata;
}
