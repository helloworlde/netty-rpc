package io.github.helloworlde.netty.rpc.client.lb;

import io.github.helloworlde.netty.rpc.client.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoundRobinLoadBalancer extends LoadBalancer {

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public Transport choose() {
        if (transports.isEmpty()) {
            throw new IllegalStateException("没有找到服务实例");
        } else if (transports.size() == 1) {
            return transports.get(0);
        }
        if (counter.incrementAndGet() >= Integer.MAX_VALUE) {
            counter.set(0);
        }
        return transports.get(counter.get() % transports.size());
    }

}
