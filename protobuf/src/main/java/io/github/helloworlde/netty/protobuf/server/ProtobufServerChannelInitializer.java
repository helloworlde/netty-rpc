package io.github.helloworlde.netty.protobuf.server;

import io.github.helloworlde.netty.protobuf.proto.HelloRequest;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class ProtobufServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new ProtobufDecoder(HelloRequest.getDefaultInstance()));
        pipeline.addLast(new ProtobufServerHandler());
    }
}
