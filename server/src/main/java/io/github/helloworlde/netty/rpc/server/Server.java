package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.interceptor.ServerInterceptor;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.server.handler.HandlerInterceptor;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.server.transport.Transport;
import io.github.helloworlde.netty.rpc.service.HeartbeatService;
import io.github.helloworlde.netty.rpc.service.impl.HeartbeatImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    List<ServerInterceptor> interceptors;

    private String serializeName = "json";

    public Server() {
    }

    public Server(String name,
                  int port,
                  String address,
                  ServiceRegistry serviceRegistry,
                  Map<String, String> metadata,
                  Registry registry,
                  List<ServerInterceptor> interceptors,
                  String serializeName) {
        this.name = name;
        this.port = port;
        this.address = address;
        this.serviceRegistry = serviceRegistry;
        this.metadata = metadata;
        this.registry = registry;
        this.interceptors = interceptors;
        this.serializeName = serializeName;
    }

    public void init() {
        serviceRegistry.addService(HeartbeatService.class, new HeartbeatImpl());

        HandlerInterceptor handlerInterceptor = new HandlerInterceptor(serviceRegistry);

        ServerCall serverCall = new ServerCall(handlerInterceptor);

        if (Objects.nonNull(this.interceptors)) {
            interceptors = this.interceptors.stream()
                                            .sorted(Comparator.comparing(ServerInterceptor::getOrder))
                                            .collect(Collectors.toList());

            for (ServerInterceptor interceptor : interceptors) {
                serverCall = new ServerCall(serverCall, interceptor);
            }
        }

        RequestProcessor requestProcessor = new RequestProcessor(serverCall);

        transport = new Transport();
        transport.doInit(serializeName, requestProcessor);
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
        log.debug("Server 注销");
        this.registry.unregister();
        this.transport.shutdown();
    }
}
