package io.github.helloworlde.netty.rpc.codec;

import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.github.helloworlde.netty.rpc.serialize.SerializeEnum;
import io.github.helloworlde.netty.rpc.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Request> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Request request, ByteBuf out) throws Exception {
        // MagicNumber + Serialize + Length + Body
        out.writeInt(Constants.PROTOCOL_MAGIC);

        Serialize serialize = SerializeEnum.JSON.getSerialize();
        byte[] requestBody = serialize.serialize(request);

        out.writeInt(SerializeEnum.JSON.getId());
        out.writeInt(requestBody.length);
        out.writeBytes(requestBody);
    }
}
