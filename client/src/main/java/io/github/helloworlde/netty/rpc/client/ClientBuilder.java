package io.github.helloworlde.netty.rpc.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Data
@Slf4j
public class ClientBuilder {

    private SocketAddress serverAddress;

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ClientBuilder forAddress(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
        return this;
    }

    public Client build() {
        return new Client(this.serverAddress);
    }
}
