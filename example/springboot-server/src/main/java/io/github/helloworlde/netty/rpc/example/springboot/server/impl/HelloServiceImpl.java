package io.github.helloworlde.netty.rpc.example.springboot.server.impl;

import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcService;
import lombok.extern.slf4j.Slf4j;

@NettyRpcService
@Slf4j
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String message) {
        log.info("新的请求: {}", message);
        return "Hello " + message;
    }
}
