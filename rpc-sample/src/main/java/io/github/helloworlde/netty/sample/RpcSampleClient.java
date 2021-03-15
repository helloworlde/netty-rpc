package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.sample.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSampleClient {

    public static void main(String[] args) {
        try {
            Client client = Client.client()
                                  .forAddress("127.0.0.1", 9091)
                                  .service(HelloService.class)
                                  .start();

            log.info("Client 启动完成");

            // ResponseFuture<Object> responseFuture = client.sendRequest("sayHello", "Hello");
            // String response = (String) responseFuture.get();
            // log.info("响应: {}", response);
            client.waiting();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
