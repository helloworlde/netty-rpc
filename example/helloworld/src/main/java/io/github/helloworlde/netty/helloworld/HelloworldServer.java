package io.github.helloworlde.netty.helloworld;

import io.github.helloworlde.netty.helloworld.service.HelloService;
import io.github.helloworlde.netty.helloworld.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.server.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldServer {

    public static void main(String[] args) throws InterruptedException {
        Server server = Server.server()
                              .port(9096)
                              .addService(HelloService.class, new HelloServiceImpl());

        server.start();
        server.awaitTermination();
    }
}
