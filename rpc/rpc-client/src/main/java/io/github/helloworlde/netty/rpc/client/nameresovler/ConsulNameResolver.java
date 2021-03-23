package io.github.helloworlde.netty.rpc.client.nameresovler;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ConsulNameResolver extends NameResolver {

    private final Consul client;

    private final HealthClient healthClient;

    public ConsulNameResolver(String host, Integer port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);

        client = Consul.builder()
                       .withHostAndPort(hostAndPort)
                       .build();

        healthClient = client.healthClient();
    }

    @Override
    public synchronized void resolve() {
        log.info("开始解析服务: {}", authority);
        ConsulResponse<List<ServiceHealth>> healthyServiceInstances = healthClient.getHealthyServiceInstances(this.authority);

        List<SocketAddress> addresses = healthyServiceInstances.getResponse()
                                                               .stream()
                                                               .map(ServiceHealth::getService)
                                                               .map(service -> new InetSocketAddress(service.getAddress(), service.getPort()))
                                                               .map(address -> (SocketAddress) address)
                                                               .collect(Collectors.toList());

        loadBalancer.updateAddress(addresses);
    }
}
