package io.github.helloworlde.netty.rpc.example.springboot.server.impl;

import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.starter.server.NettyRpcService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

@NettyRpcService
@Slf4j
public class HelloServiceImpl implements HelloService {

    @SneakyThrows
    @Override
    public String sayHello(String message) {
        log.info("新的请求: {}", message);
        Thread.sleep(RandomUtils.nextInt(0, 1000));
        return "Hello " + message;
    }
}
