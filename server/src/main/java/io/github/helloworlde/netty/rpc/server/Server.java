package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.server.transport.Transport;
import io.github.helloworlde.netty.rpc.service.HeartbeatService;
import io.github.helloworlde.netty.rpc.service.impl.HeartbeatImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Data
@Slf4j
public class Server {

    private ServiceRegistry serviceRegistry;

    private Map<String, String> metadata;

    private int port = 9090;

    private String address;

    private String name;

    private String serviceId;

    private Registry registry;

    private Transport transport;

    public Server() {
    }

    public Server(String name,
                  int port,
                  String address,
                  ServiceRegistry serviceRegistry,
                  Map<String, String> metadata,
                  Registry registry) {
        this.name = name;
        this.port = port;
        this.address = address;
        this.serviceRegistry = serviceRegistry;
        this.metadata = metadata;
        this.registry = registry;
    }

    public void init() {
        transport = new Transport();
        serviceRegistry.addService(HeartbeatService.class, new HeartbeatImpl());

        RequestProcessor requestProcessor = new RequestProcessor(serviceRegistry);

        transport.doInit(requestProcessor);
    }

    public void start() throws InterruptedException {
        if (Objects.isNull(transport)) {
            this.init();
        }
        this.port = transport.doBind(this.port);
        this.registry.register(this.name, this.address, this.port, this.metadata);
    }

    public void awaitTermination() throws InterruptedException {
        transport.awaitTermination();
    }

    public void shutdown() {
        log.info("Server 注销");
        this.registry.unregister();
        this.transport.shutdown();
    }
}
