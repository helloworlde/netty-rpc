package io.github.helloworlde.netty.protobuf.client;

import io.github.helloworlde.netty.protobuf.proto.HelloResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class ProtobufClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new ProtobufDecoder(HelloResponse.getDefaultInstance()));
        pipeline.addLast(new ProtobufClientHandler());
    }
}
