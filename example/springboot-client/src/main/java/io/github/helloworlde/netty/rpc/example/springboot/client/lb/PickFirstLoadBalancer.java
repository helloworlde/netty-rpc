package io.github.helloworlde.netty.rpc.example.springboot.client.lb;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.transport.Transport;

public class PickFirstLoadBalancer extends LoadBalancer {

    @Override
    public Transport choose() {
        if (transports.isEmpty()) {
            throw new IllegalStateException("没有找到连接成功的服务实例");
        }
        return transports.get(0);
    }

    @Override
    public String getName() {
        return "pick_first";
    }
}
