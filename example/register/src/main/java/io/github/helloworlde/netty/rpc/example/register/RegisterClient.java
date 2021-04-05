package io.github.helloworlde.netty.rpc.example.register;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.ClientBuilder;
import io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterClient {

    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.builder()
                                     .forTarget("netty-rpc-server")
                                     .nameResolver(new ConsulNameResolver("127.0.0.1", 8500))
                                     .loadBalancer(new RoundRobinLoadBalancer())
                                     .build();

        client.start();

        HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
