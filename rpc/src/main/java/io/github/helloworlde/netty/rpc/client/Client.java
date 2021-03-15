package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.Header;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class Client {

    private final AtomicLong requestIdCounter = new AtomicLong();

    private String host;

    private Integer port;

    private Class<?> service;

    private ClientHandler clientHandler;

    private Channel channel;

    private EventLoopGroup workGroup;

    public static Client client() {
        return new Client();
    }

    public Client forAddress(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public Client service(Class<?> service) {
        this.service = service;
        return this;
    }

    public Client start() {
        workGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("client-io-group"));
        try {
            clientHandler = new ClientHandler(service);
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
                               .addLast(clientHandler);
                         }
                     });


            this.channel = bootstrap.connect(host, port)
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
        return this;
    }

    public void shutdown() {
        try {
            log.info("Shutting down...");
            workGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        }
    }

    public Object sendRequest(String methodName, Object message) throws Exception {
        long requestId = requestIdCounter.incrementAndGet();
        log.info("开始发送请求: {}", requestId);
        Request request = Request.builder()
                                 .requestId(requestId)
                                 .body(message)
                                 .header(Header.builder()
                                               .serviceName(this.service.getName())
                                               .methodName(methodName)
                                               .build())
                                 .build();

        ResponseFuture<Object> responseFuture = this.clientHandler.sendRequest(request, channel);
        return responseFuture.get();
    }
}
