package io.github.helloworlde.netty.sample.service.impl;

import io.github.helloworlde.netty.sample.service.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String message) {
        return "Hello " + message;
    }
}
