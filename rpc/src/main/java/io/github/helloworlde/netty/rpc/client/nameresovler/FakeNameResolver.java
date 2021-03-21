package io.github.helloworlde.netty.rpc.client.nameresovler;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FakeNameResolver extends NameResolver {

    private int value = 0;

    public FakeNameResolver(String authority, LoadBalancer loadBalancer) {
        super(authority, loadBalancer);
    }

    @Override
    public synchronized void resolve() {
        log.info("开始解析服务: {}", authority);

        List<SocketAddress> addresses = new ArrayList<>();
        if (value % 2 == 0) {
            addresses.add(new InetSocketAddress("127.0.0.1", 9091));
        } else {
            addresses.add(new InetSocketAddress("192.168.0.105", 9091));
        }
        value += 1;
        loadBalancer.updateAddress(addresses);
    }
}
