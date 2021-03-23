package io.github.helloworlde.netty.example.service.impl;

import io.github.helloworlde.netty.example.service.HelloService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class HelloServiceImpl implements HelloService {

    private final Random random = new Random();

    @SneakyThrows
    @Override
    public String sayHello(String message) {
        log.info("新的请求: {}", message);
        // int value = random.nextInt(1000);
        // if (value > 900) {
        //     throw new RpcException("业务逻辑异常");
        // } else if (value > 800) {
        //     throw new IllegalArgumentException("参数异常");
        // } else if (value > 500) {
        //     Thread.sleep(value);
        // }
        return "Hello " + message;
    }
}
