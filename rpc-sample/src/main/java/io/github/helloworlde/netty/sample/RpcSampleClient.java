package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.sample.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSampleClient {

    public static void main(String[] args) {
        try {
            HelloService helloService = new Client<HelloService>().service(HelloService.class)
                                                                  .forAddress("127.0.0.1", 9091)
                                                                  .start();

            log.info("Client 启动完成");

            String response = helloService.sayHello("啊哈啊啊啊啊");

            log.info("返回的响应结果: {}", response);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
