package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ServerBuilder {

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private int port = 9090;

    public ServerBuilder() {
    }

    public static ServerBuilder builder() {
        return new ServerBuilder();
    }

    public ServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ServerBuilder addService(Class<?> service, Object instance) {
        serviceRegistry.addService(service, instance);
        return this;
    }

    public Server build() {
        return new Server(port, serviceRegistry);
    }
}
