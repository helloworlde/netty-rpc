package io.github.helloworlde.netty.rpc.example.springboot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyRpcServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NettyRpcServerApplication.class, args);
    }
}