package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.Transport;
import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Client<T> {

    private String host;

    private Integer port;

    private Class<T> service;

    private Transport transport;

    private EventLoopGroup workGroup;

    public Client<T> forAddress(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public Client<T> service(Class<T> service) {
        this.service = service;
        return this;
    }

    public T start() {
        workGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("client-io-group"));
        try {
            transport = new Transport();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                     .channel(NioSocketChannel.class)
                     .handler(new LoggingHandler(LogLevel.DEBUG))
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             ch.pipeline()
                               .addLast(new MessageEncoder())
                               .addLast(new MessageDecoder())
                               .addLast(transport);
                         }
                     });


            bootstrap.connect(host, port)
                     .sync()
                     .addListener(f -> {
                         log.info("Client '{}' 启动成功", service.getName());
                         Runtime.getRuntime()
                                .addShutdownHook(new Thread(this::shutdown));
                     })
                     .channel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ServiceProxy<T>(transport).newProxy(service);
    }

    public void shutdown() {
        try {
            log.info("Shutting down...");
            workGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        }
    }
}
