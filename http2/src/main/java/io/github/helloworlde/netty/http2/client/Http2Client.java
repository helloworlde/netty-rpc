package io.github.helloworlde.netty.http2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Http2Client {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                     .channel(NioSocketChannel.class)
                     .option(ChannelOption.SO_KEEPALIVE, true)
                     .handler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel ch) throws Exception {
                             Http2FrameCodec http2FrameCodec = Http2FrameCodecBuilder.forClient()
                                                                                     .initialSettings(Http2Settings.defaultSettings())
                                                                                     .build();

                             ch.pipeline()
                               .addLast(http2FrameCodec)
                               .addLast(new Http2MultiplexHandler(new SimpleChannelInboundHandler() {
                                   @Override
                                   protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                       log.info("hahhhhh");
                                   }
                               }));
                         }
                     });

            Channel channel = bootstrap.connect("127.0.0.1", 8080)
                                       .addListener(f -> log.info("客户端启动完成"))
                                       .sync()
                                       .channel();


            Http2ClientHandler http2ClientHandler = new Http2ClientHandler();
            Http2StreamChannelBootstrap streamChannelBootstrap = new Http2StreamChannelBootstrap(channel);
            Http2StreamChannel streamChannel = streamChannelBootstrap.open()
                                                                     .sync()
                                                                     .getNow();

            streamChannel.pipeline()
                         .addLast(http2ClientHandler);

            DefaultHttp2Headers headers = new DefaultHttp2Headers();
            headers.method(HttpMethod.GET.asciiName());
            headers.scheme(HttpScheme.HTTP.name());
            headers.path("/hello");

            DefaultHttp2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, true);
            streamChannel.writeAndFlush(headersFrame)
               .addListener(f -> log.info("发送 Header 完成"));
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }


    }
}
