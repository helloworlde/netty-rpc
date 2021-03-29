package io.github.helloworlde.netty.helloworld;

import io.github.helloworlde.netty.helloworld.service.HelloService;
import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldClient {

    public static void main(String[] args) throws Exception {
        Client client = Client.client()
                              .forAddress("127.0.0.1", 9096)
                              .start();

        HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
