package io.github.helloworlde.netty.rpc.registry;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsulRegistry extends Registry {

    private final Consul client;
    private final AgentClient agentClient;

    public ConsulRegistry(String host, Integer port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);

        client = Consul.builder()
                       .withHostAndPort(hostAndPort)
                       .build();

        agentClient = client.agentClient();
    }

    @Override
    public boolean register(ServiceInfo serviceInfo) {
        try {
            String check = String.format("%s:%d", serviceInfo.getAddress(), serviceInfo.getPort());
            Registration registration = ImmutableRegistration.builder()
                                                             .id(serviceInfo.getId())
                                                             .name(serviceInfo.getName())
                                                             .address(serviceInfo.getAddress())
                                                             .port(serviceInfo.getPort())
                                                             .check(Registration.RegCheck.tcp(check, 10, 2))
                                                             .meta(serviceInfo.getMetadata())
                                                             .build();
            agentClient.register(registration);
        } catch (Exception e) {
            log.error("注册服务失败: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean deRegister(ServiceInfo serviceInfo) {
        try {
            agentClient.deregister(serviceInfo.getId());
        } catch (Exception e) {
            log.error("注销服务失败: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }
}
