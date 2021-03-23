package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.Map;
import java.util.concurrent.Executor;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, ServiceDetail<?>> serviceDetailMap;

    private Executor executor;

    public ServerChannelInitializer(Map<String, ServiceDetail<?>> serviceDetailMap, Executor executor) {
        this.serviceDetailMap = serviceDetailMap;
        this.executor = executor;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // lengthFiledOffset 请求内容的偏移量；MagicNumber + Serialize = 8
        // lengthFieldLength 请求内容的长度标识偏移量 Length = 4
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          .addLast(new ReadTimeoutHandler(10))
          .addLast(new WriteTimeoutHandler(10))
          .addLast(new MessageDecoder<>(Request.class))
          .addLast(new MessageEncoder())
          .addLast(new RequestProcessor(serviceDetailMap, executor));
    }
}
