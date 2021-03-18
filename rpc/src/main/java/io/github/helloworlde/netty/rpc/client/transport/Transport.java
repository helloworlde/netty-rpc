package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Transport {

    private static EventLoopGroup workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));
    private Client client;
    private Channel channel;
    private Bootstrap bootstrap;

    public Transport(Client tClient) {
        this.client = tClient;
    }

    public void doOpen() throws Exception {
        ClientHandler handler = new ClientHandler(client);
        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .handler(new LoggingHandler(LogLevel.DEBUG))
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline()
                           .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4))
                           .addLast(new MessageEncoder())
                           .addLast(new MessageDecoder())
                           .addLast(handler);
                     }
                 });
    }

    public void doConnect(String host, int port) throws Exception {
        this.channel = bootstrap.connect(host, port)
                                .sync()
                                .addListener(f -> {
                                    log.debug("Client 启动成功");
                                })
                                .channel();
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public void shutdown() {
        this.channel.flush();
        this.channel.disconnect().addListener(f -> log.debug("Disconnect completed"));
        workerGroup.shutdownGracefully().addListener(f -> log.debug("WorkerGroup shutdown complete"));
    }

    public void write(Request request) {
        channel.writeAndFlush(request)
               .addListener(f -> {
                   if (f.isSuccess()) {
                       log.trace("请求: {} 发送完成", request.getRequestId());
                       client.sendComplete(request.getRequestId());
                   } else {
                       log.debug("请求: {} 发送失败: {} ", request.getRequestId(), f.cause().getMessage());
                       client.receiveError(request.getRequestId(), f.cause());
                   }
               });
    }
}
