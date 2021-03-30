package io.github.helloworlde.netty.register;

import io.github.helloworlde.netty.register.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import io.github.helloworlde.netty.rpc.server.Server;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterServer {

    public static void main(String[] args) throws InterruptedException {
        Server server = Server.server()
                              .port(9096)
                              .addService(HelloService.class, new HelloServiceImpl())
                              .registry(new ConsulRegistry("127.0.0.1", 8500))
                              .address("172.30.78.154")
                              .addMetadata("version", "0.1")
                              .name("netty-rpc-server");

        server.start();
        server.awaitTermination();
    }
}
