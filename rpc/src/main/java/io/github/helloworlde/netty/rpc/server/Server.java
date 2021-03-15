package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Server {
    private final Map<String, ServiceDetail<?>> serviceDetailMap = new HashMap<>();

    private int port = 9090;

    public static Server server() {
        return new Server();
    }

    public Server port(int port) {
        this.port = port;
        return this;
    }

    public Server addService(Class<?> service, Object instance) {
        if (!serviceDetailMap.containsKey(service.getName())) {
            Map<String, Method> methods = Arrays.stream(service.getMethods())
                                                .collect(Collectors.toMap(Method::getName, m -> m));


            ServiceDetail<?> serviceDetail = ServiceDetail.builder()
                                                          .service(service)
                                                          .instance(instance)
                                                          .methods(methods)
                                                          .build();

            serviceDetailMap.put(service.getName(), serviceDetail);
        }

        return this;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Channel channel = null;

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                           .channel(NioServerSocketChannel.class)
                           .handler(new LoggingHandler(LogLevel.INFO))
                           .childHandler(new ChannelInitializer<SocketChannel>() {
                               @Override
                               protected void initChannel(SocketChannel ch) throws Exception {
                                   ch.pipeline()
                                     .addLast(new MessageDecoder())
                                     .addLast(new MessageEncoder())
                                     .addLast(new RequestProcessor(serviceDetailMap));
                               }
                           });

            ChannelFuture channelFuture = serverBootstrap.bind(port)
                                                         .addListener(f -> {
                                                             if (f.isSuccess()) {
                                                                 log.info("Server 启动完成");
                                                             } else {
                                                                 log.error("Server 启动失败");
                                                             }
                                                         });

            channel = channelFuture.channel();
            channel.closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("Server 初始化失败: {}", e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
