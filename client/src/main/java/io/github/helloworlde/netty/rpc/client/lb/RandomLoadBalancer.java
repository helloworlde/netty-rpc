package io.github.helloworlde.netty.rpc.client.lb;

import io.github.helloworlde.netty.rpc.client.transport.Transport;

import java.util.Random;

public class RandomLoadBalancer extends LoadBalancer {

    private final Random random = new Random();

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public Transport choose() {
        if (transports.isEmpty()) {
            throw new IllegalStateException("没有找到连接成功的服务实例");
        } else if (transports.size() == 1) {
            return transports.get(0);
        }
        return transports.get(random.nextInt(transports.size()));
    }

}
