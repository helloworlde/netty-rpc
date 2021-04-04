package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.registry.NoopRegistry;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.server.interceptor.ServerInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Slf4j
public class ServerBuilder {

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private Map<String, String> metadata = new HashMap<>();

    private int port = 9090;

    private String address;

    private String name;

    private Registry registry;

    private List<ServerInterceptor> interceptors = new ArrayList<>();

    public ServerBuilder() {
    }

    public static ServerBuilder builder() {
        return new ServerBuilder();
    }

    public ServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ServerBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ServerBuilder address(String address) {
        this.address = address;
        return this;
    }

    public ServerBuilder registry(Registry registry) {
        this.registry = registry;
        return this;
    }

    public ServerBuilder addMetadata(String name, String value) {
        this.metadata.put(name, value);
        return this;
    }


    public ServerBuilder addService(Class<?> service, Object instance) {
        serviceRegistry.addService(service, instance);
        return this;
    }

    public ServerBuilder addInterceptor(ServerInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    public Server build() {
        if (Objects.isNull(address)) {
            address = InetAddress.getLoopbackAddress().getHostAddress();
        }
        if (Objects.isNull(registry)) {
            registry = new NoopRegistry();
        }

        return new Server(name, port, address, serviceRegistry, metadata, registry, interceptors);
    }
}
