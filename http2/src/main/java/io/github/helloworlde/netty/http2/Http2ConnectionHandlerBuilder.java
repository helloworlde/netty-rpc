package io.github.helloworlde.netty.http2;

import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.logging.LogLevel;

public class Http2ConnectionHandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2ServerHandler, Http2ConnectionHandlerBuilder> {

    private static final Http2FrameLogger logger = new Http2FrameLogger(LogLevel.INFO, Http2ServerHandler.class);

    public Http2ConnectionHandlerBuilder() {
        frameLogger(logger);
    }

    @Override
    protected Http2ServerHandler build() {
        return super.build();
    }

    @Override
    protected Http2ServerHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) throws Exception {
        Http2ServerHandler handler = new Http2ServerHandler(decoder, encoder, initialSettings);
        frameListener(handler);
        return handler;
    }
}
