package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.transport.ClientChannelInitializer;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.serialize.JsonSerialize;
import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Objects;

@Data
@Slf4j
public class Client {

    private EventLoopGroup workerGroup;

    private SocketAddress serverAddress;

    private Transport transport;

    public Client(SocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Client init() {
        Bootstrap bootstrap = new Bootstrap();
        ClientHandler handler = new ClientHandler();
        workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));

        Serialize serialize = new JsonSerialize();

        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new LoggingHandler(LogLevel.TRACE))
                 .handler(new ClientChannelInitializer(serialize, handler));

        this.transport = new Transport(this.serverAddress, bootstrap);
        return this;
    }

    public void start() throws Exception {
        if (Objects.isNull(workerGroup)) {
            this.init();
        }
        log.debug("Client starting...");
        this.transport.doConnect();
    }

    public void shutdown() {
        try {
            log.debug("Start shutting down...");
            this.workerGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        } finally {
            log.debug("Shutting down completed.");
        }
    }

    public Transport getTransport() {
        return transport;
    }
}
