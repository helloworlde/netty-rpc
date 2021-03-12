package io.github.helloworlde.netty.http2;

import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerUpgradeHandlerFactory implements HttpServerUpgradeHandler.UpgradeCodecFactory {

    @Override
    public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            log.info("H2C 协议升级");
            return new Http2ServerUpgradeCodec(new Http2ConnectionHandlerBuilder().build());
        }
        return null;
    }
}
