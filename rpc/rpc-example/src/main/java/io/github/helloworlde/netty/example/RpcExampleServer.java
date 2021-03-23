package io.github.helloworlde.netty.example;

import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.example.service.HelloService;
import io.github.helloworlde.netty.example.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcExampleServer {

    public static void main(String[] args) throws InterruptedException {
        Server server = Server.server()
                              .address("172.30.78.154")
                              .port(9096)
                              .name("RPC_SERVER")
                              .registry(new ConsulRegistry("localhost", 8500))
                              .addMetadata("env", "test")
                              .addMetadata("version", "1.0.0")
                              .addService(HelloService.class, new HelloServiceImpl());

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}
