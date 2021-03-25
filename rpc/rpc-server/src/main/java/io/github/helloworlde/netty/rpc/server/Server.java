package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.registry.ServiceInfo;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.server.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Server {


    private final ServiceRegistry serviceRegistry = new ServiceRegistry();

    private final Map<String, String> metadata = new HashMap<>();

    private int port = 9090;

    private String address = "127.0.0.1";

    private String name;

    private String serviceId;

    private Registry registry;

    private Transport transport;

    public static Server server() {
        return new Server();
    }

    public Server port(int port) {
        this.port = port;
        return this;
    }

    public Server name(String name) {
        this.name = name;
        return this;
    }

    public Server address(String address) {
        this.address = address;
        return this;
    }


    public Server addMetadata(String name, String value) {
        this.metadata.put(name, value);
        return this;
    }


    public Server registry(Registry registry) {
        this.registry = registry;
        return this;
    }

    public Server addService(Class<?> service, Object instance) {
        serviceRegistry.addService(service, instance);
        return this;
    }

    public void start() throws InterruptedException {
        transport = new Transport();

        RequestProcessor requestProcessor = new RequestProcessor(serviceRegistry);

        transport.doInit(requestProcessor);
        this.port = transport.doBind(this.port);

        if (Objects.nonNull(this.registry)) {
            doRegistry();
        }
    }

    public void awaitTermination() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        transport.awaitTermination();
    }

    private void doRegistry() {
        try {
            this.serviceId = String.format("%s-%s-%d", this.name, this.address, this.port);
            ServiceInfo serviceInfo = ServiceInfo.builder()
                                                 .id(this.serviceId)
                                                 .name(this.name)
                                                 .port(this.port)
                                                 .address(this.address)
                                                 .metadata(metadata)
                                                 .build();
            log.info("Server 注册: {}", serviceInfo);
            this.registry.register(serviceInfo);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        log.info("Server 注销");
        this.registry.deregister(ServiceInfo.builder()
                                            .id(this.serviceId)
                                            .build());
        this.transport.shutdown();
    }
}
