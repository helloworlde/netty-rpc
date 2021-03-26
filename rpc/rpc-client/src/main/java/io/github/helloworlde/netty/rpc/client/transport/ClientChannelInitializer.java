package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.Response;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ClientHandler handler;

    public ClientChannelInitializer(ClientHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // lengthFiledOffset 请求内容的偏移量；MagicNumber + Serialize = 8
        // lengthFieldLength 请求内容的长度标识偏移量 Length = 4
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          // .addLast(new ReadTimeoutHandler(10))
          // .addLast(new WriteTimeoutHandler(10))
          .addLast(new MessageEncoder())
          .addLast(new MessageDecoder<>(Response.class))
          .addLast(handler);
    }
}
