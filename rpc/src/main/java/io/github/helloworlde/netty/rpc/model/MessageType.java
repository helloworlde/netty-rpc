package io.github.helloworlde.netty.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum MessageType {
    REQUEST(1),
    RESPONSE(2),
    ;
    private int type;

    public static MessageType fromType(int type) {
        return Arrays.stream(values())
                     .filter(m -> m.getType() == type)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown MessageType: " + type));
    }
}
