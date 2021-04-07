package io.github.helloworlde.netty.rpc.client.nameresovler;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ConsulNameResolver extends NameResolver {

    private final Consul client;

    private final HealthClient healthClient;

    private ScheduledExecutorService executor;

    public ConsulNameResolver(String host, Integer port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);

        client = Consul.builder()
                       .withHostAndPort(hostAndPort)
                       .build();

        healthClient = client.healthClient();
    }

    @Override
    public void start() {
        super.start();
        this.executor = new ScheduledThreadPoolExecutor(2, new DefaultThreadFactory("name-resolver"));
        this.executor.scheduleAtFixedRate(this::refresh, 5, 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    @Override
    public synchronized void refresh() {
        log.debug("开始解析服务: {}", authority);

        ConsulResponse<List<ServiceHealth>> healthyServiceInstances = healthClient.getHealthyServiceInstances(this.authority);

        List<SocketAddress> addresses = healthyServiceInstances.getResponse()
                                                               .stream()
                                                               .map(ServiceHealth::getService)
                                                               .map(service -> new InetSocketAddress(service.getAddress(), service.getPort()))
                                                               .map(address -> (SocketAddress) address)
                                                               .collect(Collectors.toList());

        loadBalancer.onResult(addresses);
    }
}
