package io.github.helloworlde.netty.rpc.server;

import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.registry.ServiceInfo;
import io.github.helloworlde.netty.rpc.server.handler.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class Server {

    private final Map<String, ServiceDetail<?>> serviceDetailMap = new HashMap<>();

    private int port = 9090;

    private String address = "127.0.0.1";

    private String name;

    private String serviceId;

    private Map<String, String> metadata = new HashMap<>();

    private Registry registry;

    public static Server server() {
        return new Server();
    }

    public Server port(int port) {
        this.port = port;
        return this;
    }

    public Server name(String name) {
        this.name = name;
        return this;
    }

    public Server address(String address) {
        this.address = address;
        return this;
    }


    public Server addMetadata(String name, String value) {
        this.metadata.put(name, value);
        return this;
    }


    public Server registry(Registry registry) {
        this.registry = registry;
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
        this.serviceId = UUID.randomUUID().toString();
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
                           .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                           .handler(new LoggingHandler(LogLevel.DEBUG))
                           .childHandler(new ServerChannelInitializer(serviceDetailMap, executor));

            ChannelFuture channelFuture = serverBootstrap.bind(port)
                                                         .addListener(f -> {
                                                             if (f.isSuccess()) {
                                                                 log.debug("Server 启动完成");
                                                                 if (Objects.nonNull(this.registry)) {
                                                                     doRegistry();
                                                                 }
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

    private void doRegistry() {
        try {
            ServiceInfo serviceInfo = ServiceInfo.builder()
                                                 .id(this.serviceId)
                                                 .name(this.name)
                                                 .port(this.port)
                                                 .address(this.address)
                                                 .metadata(metadata)
                                                 .build();
            this.registry.register(serviceInfo);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        this.registry.deRegister(ServiceInfo.builder()
                                            .id(this.serviceId)
                                            .build());
    }
}
