package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.heartbeat.HeartbeatTask;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Objects;

@Slf4j
public class Transport {

    private Channel channel;

    private Bootstrap bootstrap;

    private ClientHandler handler;

    private SocketAddress address;

    private HeartbeatTask heartbeatTask;

    private boolean enableHeartbeat;

    public Transport(SocketAddress address, Bootstrap bootstrap, boolean enableHeartbeat) {
        this.address = address;
        this.bootstrap = bootstrap;
        this.enableHeartbeat = enableHeartbeat;
    }

    public void doConnect() throws Exception {
        if (Objects.nonNull(channel) && channel.isActive()) {
            return;
        }
        log.debug("开始连接: {}", this.address);

        ChannelFuture future = bootstrap.connect(address)
                                        .sync()
                                        .await();

        if (future.isSuccess()) {
            log.debug("连接: {} 成功", this.address);
            if (enableHeartbeat) {
                this.heartbeatTask = new HeartbeatTask(this);
            }
            this.channel = future.channel();
            log.debug("channel: {}", channel);
            this.handler = this.channel.pipeline().get(ClientHandler.class);
        } else {
            throw new IllegalStateException(String.format("连接 %s 失败", this.address));
        }
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void shutdown() {
        log.debug("开始关闭连接: {}", this.address);
        this.channel.flush();
        if (enableHeartbeat) {
            this.heartbeatTask.shutdown();
        }
        this.channel.disconnect();
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {

        log.debug("请求: {}", this.address);
        this.handler.write(request, responseFuture);
    }

    @Override
    public String toString() {
        return "Transport{" +
                "channel=" + channel +
                '}';
    }
}
