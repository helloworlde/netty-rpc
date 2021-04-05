package io.github.helloworlde.netty.rpc.example.register;

import io.github.helloworlde.netty.rpc.example.register.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterServer {

    public static void main(String[] args) throws InterruptedException {
        Server server = ServerBuilder.builder()
                                     .port(9096)
                                     .addService(HelloService.class, new HelloServiceImpl())
                                     .registry(new ConsulRegistry("127.0.0.1", 8500))
                                     .address("172.30.78.154")
                                     .addMetadata("version", "0.1")
                                     .name("netty-rpc-server")
                                     .build();

        server.start();
        server.awaitTermination();
    }
}
