package io.github.helloworlde.netty.rpc.example.springboot.client.controller;


import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {

    @NettyRpcClient("netty-rpc-server")
    private HelloService helloService;

    @GetMapping("/hello")
    public Object hello(String message) {
        return helloService.sayHello(message);
    }
}
