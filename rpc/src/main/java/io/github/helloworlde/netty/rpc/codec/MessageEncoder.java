package io.github.helloworlde.netty.rpc.codec;

import io.github.helloworlde.netty.rpc.model.MessageType;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.serialize.Serialize;
import io.github.helloworlde.netty.rpc.serialize.SerializeEnum;
import io.github.helloworlde.netty.rpc.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Object> {

    /**
     * Protocol: MagicNumber + MessageType + Serialize + Length + Body
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        log.info("Encode");
        out.writeInt(Constants.PROTOCOL_MAGIC);

        // 消息类型
        MessageType messageType;
        if (msg instanceof Request) {
            messageType = MessageType.REQUEST;
        } else if (msg instanceof Response) {
            messageType = MessageType.RESPONSE;
        } else {
            throw new IllegalArgumentException("Unknown message type");
        }

        out.writeInt(messageType.getType());

        // 序列化类型
        Serialize serialize = SerializeEnum.JSON.getSerialize();
        out.writeInt(SerializeEnum.JSON.getId());

        // Body
        byte[] requestBody = serialize.serialize(msg);
        out.writeInt(requestBody.length);
        out.writeBytes(requestBody);
    }
}
