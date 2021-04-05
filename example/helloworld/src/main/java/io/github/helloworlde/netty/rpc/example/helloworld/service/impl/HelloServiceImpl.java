package io.github.helloworlde.netty.rpc.example.helloworld.service.impl;

import io.github.helloworlde.netty.rpc.example.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String message) {
        log.info("新的请求: {}", message);
        return "Hello " + message;
    }
}
