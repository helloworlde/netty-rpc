package io.github.helloworlde.netty.rpc.client.transport;

import io.netty.bootstrap.Bootstrap;

import java.net.SocketAddress;

public class TransportFactory {

    private final Bootstrap bootstrap;

    private final boolean enableHeartbeat;

    public TransportFactory(Bootstrap bootstrap, boolean enableHeartbeat) {
        this.bootstrap = bootstrap;
        this.enableHeartbeat = enableHeartbeat;
    }

    public Transport createTransport(SocketAddress address) {
        return new Transport(address, bootstrap, enableHeartbeat);
    }
}
