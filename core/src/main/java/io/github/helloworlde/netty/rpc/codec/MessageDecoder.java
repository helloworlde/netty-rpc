package io.github.helloworlde.netty.rpc.codec;

import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.github.helloworlde.netty.rpc.serialize.SerializeProvider;
import io.github.helloworlde.netty.rpc.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder<T> extends ByteToMessageDecoder {

    private final Class<T> decodeClass;

    public MessageDecoder(Class<T> decodeClass) {
        this.decodeClass = decodeClass;
    }

    /**
     * Protocol: MagicNumber + Serialize + Length + Body
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.trace("Decode message");
        int protocol = in.readInt();
        if (Constants.PROTOCOL_MAGIC != protocol) {
            log.warn("协议无法识别: {}", protocol);
            ctx.close();
        }

        // 序列化类型
        int serializeType = in.readInt();
        Serialize serialize = SerializeProvider.getSerialize(serializeType);

        // Body
        int length = in.readInt();
        byte[] bodyBytes = new byte[length];
        in.readBytes(bodyBytes);

        T result = serialize.deserialize(bodyBytes, decodeClass);
        out.add(result);
    }
}
