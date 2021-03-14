package io.github.helloworlde.netty.rpc.codec;

import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.github.helloworlde.netty.rpc.serialize.SerializeEnum;
import io.github.helloworlde.netty.rpc.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // MagicNumber + Serialize + Length + Body
        int protocol = in.readInt();
        if (Constants.PROTOCOL_MAGIC != protocol) {
            log.info("协议不正确");
            ctx.close();
        }

        int serializeType = in.readInt();
        Serialize serialize = SerializeEnum.getById(serializeType);

        int length = in.readInt();
        byte[] bodyBytes = new byte[length];
        in.readBytes(bodyBytes);

        Request request = serialize.deserialize(bodyBytes, Request.class);

        out.add(request);
    }
}
