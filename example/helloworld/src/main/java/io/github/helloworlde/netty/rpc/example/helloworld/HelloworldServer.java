package io.github.helloworlde.netty.rpc.example.helloworld;

import io.github.helloworlde.netty.rpc.example.helloworld.service.HelloService;
import io.github.helloworlde.netty.rpc.example.helloworld.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldServer {

    public static void main(String[] args) throws InterruptedException {

        Server server = ServerBuilder.builder()
                                     .port(9090)
                                     .addService(HelloService.class, new HelloServiceImpl())
                                     .build();

        server.start();
        server.awaitTermination();
    }
}
