package io.github.helloworlde.netty.rpc.codec;

import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.github.helloworlde.netty.rpc.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Object> {

    private final Serialize serialize;

    public MessageEncoder(Serialize serialize) {
        this.serialize = serialize;
    }

    /**
     * Protocol: MagicNumber + Serialize + Length + Body
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        log.trace("Encode message");
        out.writeInt(Constants.PROTOCOL_MAGIC);

        // 序列化类型
        out.writeInt(serialize.getId());

        // Body
        byte[] requestBody = serialize.serialize(msg);
        out.writeInt(requestBody.length);
        out.writeBytes(requestBody);
    }
}
