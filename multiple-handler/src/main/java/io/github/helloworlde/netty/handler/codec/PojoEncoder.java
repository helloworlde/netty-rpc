package io.github.helloworlde.netty.handler.codec;

import io.github.helloworlde.netty.handler.model.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class PojoEncoder extends MessageToByteEncoder<CustomMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        log.info("Encode message");
        // 顺序写入 id 和 timestamp
        out.writeInt(PojoDecoder.CUSTOM_PROTOCOL);
        out.writeLong(msg.getId());
        out.writeLong(msg.getTimestamp());
        // 写入字符串
        out.writeBytes(msg.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}
