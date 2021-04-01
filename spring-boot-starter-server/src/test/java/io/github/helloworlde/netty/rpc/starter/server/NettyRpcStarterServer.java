package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.starter.common.TestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyRpcStarterServer {

    public static void main(String[] args) {
        SpringApplication.run(NettyRpcStarterServer.class);
    }

    // @Bean
    // public Registry registry() {
    //     return new ConsulRegistry("127.0.0.1", 8500);
    // }
}

@NettyRpcService
class TestServiceImpl implements TestService {

    @Override
    public String test(String message) {
        return "This is a test, message is :" + message;
    }
}