package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.ServiceProxy;
import io.github.helloworlde.netty.sample.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcSampleClient {

    public static void main(String[] args) {
        try {
            Client client = new Client().forAddress("127.0.0.1", 9091)
                                        .start();
            HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

            log.info("Client 启动完成");
            String response = helloService.sayHello("啊哈啊啊啊啊");
            log.info("返回的响应结果: {}", response);

            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
