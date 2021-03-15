package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.sample.service.HelloService;
import io.github.helloworlde.netty.sample.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSampleServer {

    public static void main(String[] args) throws InterruptedException {
        Server.server()
              .addService(HelloService.class, new HelloServiceImpl())
              .start();
    }
}
