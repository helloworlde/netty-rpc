package io.github.helloworlde.netty.rpc.registry;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ConsulRegistry extends Registry {

    private final Consul client;
    private final AgentClient agentClient;

    private String serviceId;

    public ConsulRegistry(String host, Integer port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);

        client = Consul.builder()
                       .withHostAndPort(hostAndPort)
                       .build();

        agentClient = client.agentClient();
    }

    @Override
    public boolean register(String name, String address, int port, Map<String, String> metadata) {
        try {
            this.serviceId = String.format("%s-%s-%d", name, address, port);
            String check = String.format("%s:%d", address, port);
            Registration registration = ImmutableRegistration.builder()
                                                             .id(serviceId)
                                                             .name(name)
                                                             .address(address)
                                                             .port(port)
                                                             .check(Registration.RegCheck.tcp(check, 10, 2))
                                                             .meta(metadata)
                                                             .build();
            agentClient.register(registration);
        } catch (Exception e) {
            log.error("注册服务失败: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean unregister() {
        try {
            agentClient.deregister(this.serviceId);
        } catch (Exception e) {
            log.error("注销服务失败: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }
}
