package io.github.helloworlde.netty.rpc.example.helloworld;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.ClientBuilder;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import io.github.helloworlde.netty.rpc.example.helloworld.interceptor.ClientInterceptorOne;
import io.github.helloworlde.netty.rpc.example.helloworld.interceptor.ClientInterceptorTwo;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldClient {

    public static void main(String[] args) throws Exception {

        Client client = ClientBuilder.builder()
                                     .forAddress("127.0.0.1", 9096)
                                     .addInterceptor(new ClientInterceptorOne())
                                     .addInterceptor(new ClientInterceptorTwo())
                                     .build();

        client.start();

        HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
