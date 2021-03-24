package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.registry.ServiceInfo;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.github.helloworlde.netty.rpc.server.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class Server {

    private final Map<String, ServiceDetail<?>> serviceDetailMap = new HashMap<>();

    private int port = 9090;

    private String address = "127.0.0.1";

    private String name;

    private String serviceId;

    private Map<String, String> metadata = new HashMap<>();

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
        if (!serviceDetailMap.containsKey(service.getName())) {
            Map<String, Method> methods = Arrays.stream(service.getMethods())
                                                .collect(Collectors.toMap(Method::getName, m -> m));


            ServiceDetail<?> serviceDetail = ServiceDetail.builder()
                                                          .service(service)
                                                          .instance(instance)
                                                          .methods(methods)
                                                          .build();

            serviceDetailMap.put(service.getName(), serviceDetail);
        }

        return this;
    }

    public void start() {
        this.serviceId = UUID.randomUUID().toString();
        Thread thread = new Thread(this::startUp);
        thread.start();
    }

    private void startUp() {
        try {
            transport = new Transport();

            RequestProcessor requestProcessor = new RequestProcessor(serviceDetailMap);

            transport.doInit(requestProcessor);
            this.port = transport.doBind(this.port);

            if (Objects.nonNull(this.registry)) {
                doRegistry();
            }
            transport.waiting();
        } catch (Exception e) {
            log.error("Server 初始化失败: {}", e.getMessage(), e);
        } finally {
            transport.shutdown();
        }
    }


    private void doRegistry() {
        try {
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
        this.registry.deRegister(ServiceInfo.builder()
                                            .id(this.serviceId)
                                            .build());
        this.transport.shutdown();
    }
}
