package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Objects;

@Slf4j
public class Transport {

    private Channel channel;

    private Bootstrap bootstrap;

    private ClientHandler handler;

    private SocketAddress address;

    public Transport(SocketAddress address, Bootstrap bootstrap) {
        this.address = address;
        this.bootstrap = bootstrap;
    }

    public void doConnect() throws Exception {
        if (Objects.nonNull(channel) && channel.isActive()) {
            return;
        }
        log.info("开始连接: {}", this.address);

        this.channel = bootstrap.connect(address)
                                .sync()
                                .addListener(f -> log.info("连接: {} 成功", this.address))
                                .channel();
        log.info("channel: {}", channel);
        this.handler = this.channel.pipeline().get(ClientHandler.class);
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void shutdown() {
        log.info("开始关闭连接: {}", this.address);
        this.channel.flush();
        this.channel.disconnect().addListener(f -> log.info("Disconnect completed"));
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {

        log.info("请求: {}", this.address);
        this.handler.write(request, responseFuture);
    }

    @Override
    public String toString() {
        return "Transport{" +
                "channel=" + channel +
                '}';
    }
}
