package io.github.helloworlde.netty.rpc.example.springboot.client.controller;


import io.github.helloworlde.netty.rpc.context.Context;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.starter.client.NettyRpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ExampleController {

    @NettyRpcClient("netty-rpc-server")
    private HelloService helloService;

    @GetMapping("/hello")
    public Object hello(String message) {
        return helloService.sayHello(message);
    }

    @GetMapping("/timeout")
    public Object timeout(String message, Long timeout) {
        Context.current().withTimeout(timeout);
        Context.current().withAttribute("startTime", LocalDateTime.now());
        return helloService.sayHello(message);
    }
}
