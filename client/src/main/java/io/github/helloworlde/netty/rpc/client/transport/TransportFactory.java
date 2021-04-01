package io.github.helloworlde.netty.rpc.client.transport;

import io.netty.bootstrap.Bootstrap;

import java.net.SocketAddress;

public class TransportFactory {

    private final Bootstrap bootstrap;

    public TransportFactory(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Transport createTransport(SocketAddress address) {
        return new Transport(address, bootstrap);
    }
}
