package io.github.helloworlde.netty.register;

import io.github.helloworlde.netty.register.service.HelloService;
import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterClient {

    public static void main(String[] args) throws Exception {
        Client client = Client.client()
                              .forTarget("netty-rpc-server")
                              .nameResolver(new ConsulNameResolver("127.0.0.1", 8500))
                              .loadBalancer(new RoundRobinLoadBalancer())
                              .start();

        HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
