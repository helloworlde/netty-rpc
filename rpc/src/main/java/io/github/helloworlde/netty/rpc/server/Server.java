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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
        Thread thread = new Thread(this::startUp);
        thread.start();
    }

    private void startUp() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("accept-group"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("io-event-group"));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("business-group"));
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
                                     .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4))
                                     .addLast(new MessageDecoder())
                                     .addLast(new MessageEncoder())
                                     .addLast(new RequestProcessor(serviceDetailMap, executor));
                               }
                           });

            ChannelFuture channelFuture = serverBootstrap.bind(port)
                                                         .addListener(f -> {
                                                             if (f.isSuccess()) {
                                                                 log.debug("Server 启动完成");
                                                             } else {
                                                                 log.debug("Server 启动失败");
                                                             }
                                                         });

            channel = channelFuture.channel();
            channel.closeFuture().sync();

        } catch (Exception e) {
            log.error("Server 初始化失败: {}", e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
