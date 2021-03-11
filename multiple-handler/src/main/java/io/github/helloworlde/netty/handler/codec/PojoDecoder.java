package io.github.helloworlde.netty.handler.codec;

import io.github.helloworlde.netty.handler.model.CustomMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PojoDecoder extends ByteToMessageDecoder {

    public static final int CUSTOM_PROTOCOL = 2333;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 记录当前的读指针位置
        in.markReaderIndex();
        int protocol = in.readInt();
        if (protocol != CUSTOM_PROTOCOL) {
            log.info("不是自定义协议，交由后续处理");
            // 重置读指针，避免后续读取出现乱码
            in.resetReaderIndex();
            // 移除当前 handler
            ctx.channel().pipeline().remove(this);
            return;
        }

        log.info("Decode message");
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
