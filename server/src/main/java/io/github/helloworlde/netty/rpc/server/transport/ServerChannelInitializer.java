package io.github.helloworlde.netty.rpc.server.transport;

import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.Executor;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final RequestProcessor processor;

    private final Executor executor;

    public ServerChannelInitializer(RequestProcessor processor, Executor executor) {
        this.processor = processor;
        this.executor = executor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // lengthFiledOffset 请求内容的偏移量；MagicNumber + Serialize = 8
        // lengthFieldLength 请求内容的长度标识偏移量 Length = 4
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          // .addLast(new ReadTimeoutHandler(10))
          // .addLast(new WriteTimeoutHandler(10))
          .addLast(new MessageDecoder<>(Request.class))
          .addLast(new MessageEncoder())
          .addLast(new ServerHandler(this.processor, this.executor));
    }
}
