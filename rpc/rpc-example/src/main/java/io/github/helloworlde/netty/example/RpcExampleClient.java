package io.github.helloworlde.netty.example;

import io.github.helloworlde.netty.example.service.HelloService;
import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RpcExampleClient {
    static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) {
        Client client = null;
        try {
            client = new Client()
                    .forTarget("RPC_SERVER")
                    .nameResolver(new ConsulNameResolver("127.0.0.1", 8500))
                    // .forAddress("127.0.0.1", 9091)
                    .start();

            log.info("Client 启动完成");


            HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

            String response = helloService.sayHello("啊哈啊啊啊啊 " + counter.getAndIncrement());
            log.info("返回的响应结果: {}", response);

            asyncMultiple(helloService);
            syncMultiple(helloService);

            Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));

        } catch (Exception e) {
            log.info("异常: {}", e.getMessage(), e);
        } finally {
            client.shutdown();
        }
    }

    private static void syncMultiple(HelloService helloService) {
        String response;
        for (int i = 0; i < 10000; i++) {
            try {
                response = helloService.sayHello("啊哈啊啊啊啊 " + counter.getAndIncrement());
                log.info(response);
                Thread.sleep(10);
            } catch (Exception e) {
                // e.printStackTrace();
                log.info("调用失败: {}", e.getMessage(), e);
            }
        }
    }

    private static void asyncMultiple(HelloService helloService) throws InterruptedException, java.util.concurrent.ExecutionException {

        List<CompletableFuture<String>> futureList = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                log.info("发送请求: {}", counter.getAndIncrement());
                return helloService.sayHello("" + counter.get());
            });
            futureList.add(future);
        }

        for (CompletableFuture<String> future : futureList) {
            String response = future.get();
            log.info("返回的响应结果: {}", response);
        }
    }
}
