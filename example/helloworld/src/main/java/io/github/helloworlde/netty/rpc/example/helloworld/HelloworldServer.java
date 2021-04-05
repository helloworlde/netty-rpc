package io.github.helloworlde.netty.rpc.example.helloworld;

import io.github.helloworlde.netty.rpc.example.helloworld.interceptor.ServerInterceptorOne;
import io.github.helloworlde.netty.rpc.example.helloworld.interceptor.ServerInterceptorTwo;
import io.github.helloworlde.netty.rpc.example.helloworld.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldServer {

    public static void main(String[] args) throws InterruptedException {

        Server server = ServerBuilder.builder()
                                     .port(9096)
                                     .addService(HelloService.class, new HelloServiceImpl())
                                     .addInterceptor(new ServerInterceptorOne())
                                     .addInterceptor(new ServerInterceptorTwo())
                                     .build();

        server.start();
        server.awaitTermination();
    }
}
