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
        EventLoopGroup workGroup = new NioEventLoopGroup();
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


            Channel channel = bootstrap.connect(host, port)
                                       .addListener(f -> log.info("Client {} 启动成功", service.getName()))
                                       .channel();

            this.channel = channel;
            channel.closeFuture()
                   .sync();
        } catch (Exception e) {
            log.error("Client {} 错误: {}", service.getName(), e.getMessage());
        } finally {
            workGroup.shutdownGracefully();
        }

        return this;
    }

    public void waiting() {
        try {
            channel.closeFuture()
                   .sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ResponseFuture<Object> sendRequest(String methodName, Object message) {
        log.info("开始发送请求");
        long requestId = requestIdCounter.incrementAndGet();

        Request request = Request.builder()
                                 .requestId(requestId)
                                 .body(message)
                                 .header(Header.builder()
                                               .serviceName(this.service.getName())
                                               .methodName(methodName)
                                               .build())
                                 .build();

        return this.clientHandler.sendRequest(request, channel);
    }
}
