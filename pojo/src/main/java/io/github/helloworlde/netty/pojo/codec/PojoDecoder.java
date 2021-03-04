package io.github.helloworlde.netty.pojo.codec;

import io.github.helloworlde.netty.pojo.model.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PojoDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 顺序读取 id 和 timestamp，读取和写入要一致
        Long id = in.readLong();
        Long timestamp = in.readLong();

        // 读取字符串
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        // 构建对象实例
        CustomMessage message = CustomMessage.builder()
                                             .id(id)
                                             .message(new String(bytes))
                                             .timestamp(timestamp)
                                             .build();
        out.add(message);
    }
}
