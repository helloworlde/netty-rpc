package io.github.helloworlde.netty.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        HttpServerCodec serverCodec = new HttpServerCodec();
        HttpServerUpgradeHandler httpServerUpgradeHandler = new HttpServerUpgradeHandler(serverCodec, new HttpServerUpgradeHandlerFactory());
        CleartextHttp2ServerUpgradeHandler cleartextHttp2ServerUpgradeHandler = new CleartextHttp2ServerUpgradeHandler(serverCodec, httpServerUpgradeHandler, new Http2ConnectionHandlerBuilder().build());

        pipeline.addLast(cleartextHttp2ServerUpgradeHandler);
        pipeline.addLast(new SimpleChannelInboundHandler<HttpMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
                log.info("HTTP1 Replacer");
                ChannelPipeline channelPipeline = ctx.pipeline();
                channelPipeline.addAfter(ctx.name(), null, new Http1ServerHandler());
                channelPipeline.replace(this, null, new HttpObjectAggregator(Integer.MAX_VALUE));
                ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
        });
    }


}
