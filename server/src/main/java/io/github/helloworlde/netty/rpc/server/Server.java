package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.server.transport.Transport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class Server {

    private ServiceRegistry serviceRegistry;

    private int port = 9090;

    private Transport transport;


    public Server(int port, ServiceRegistry serviceRegistry) {
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    public void init() {
        RequestProcessor requestProcessor = new RequestProcessor(this.serviceRegistry);

        transport = new Transport();
        transport.doInit(requestProcessor);
    }

    public void start() throws InterruptedException {
        if (Objects.isNull(transport)) {
            this.init();
        }
        this.port = transport.doBind(this.port);
    }

    public void awaitTermination() throws InterruptedException {
        transport.awaitTermination();
    }

    public void shutdown() {
        log.debug("Server 注销");
        this.transport.shutdown();
    }
}
