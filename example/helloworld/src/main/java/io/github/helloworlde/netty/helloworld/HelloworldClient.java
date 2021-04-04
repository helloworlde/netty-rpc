package io.github.helloworlde.netty.helloworld;

import io.github.helloworlde.netty.helloworld.interceptor.ClientInterceptorOne;
import io.github.helloworlde.netty.helloworld.interceptor.ClientInterceptorTwo;
import io.github.helloworlde.netty.opentelemetry.ClientTraceInterceptor;
import io.github.helloworlde.netty.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.ClientBuilder;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloworldClient {

    public static void main(String[] args) throws Exception {

        OpenTelemetry telemetry = OpenTelemetryConfig.getOpenTelemetry("客户端", "127.0.0.1", 14250);

        Client client = ClientBuilder.builder()
                                     .forAddress("127.0.0.1", 9096)
                                     .addInterceptor(new ClientInterceptorOne())
                                     .addInterceptor(new ClientInterceptorTwo())
                                     .addInterceptor(new ClientTraceInterceptor(telemetry))
                                     .build();

        client.start();

        HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
