package io.github.helloworlde.netty.pojo.codec;

import io.github.helloworlde.netty.pojo.model.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class PojoEncoder extends MessageToByteEncoder<CustomMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CustomMessage msg, ByteBuf out) throws Exception {
        // 顺序写入 id 和 timestamp
        out.writeLong(msg.getId());
        out.writeLong(msg.getTimestamp());
        // 写入字符串
        out.writeBytes(msg.getMessage().getBytes(StandardCharsets.UTF_8));
    }
}
