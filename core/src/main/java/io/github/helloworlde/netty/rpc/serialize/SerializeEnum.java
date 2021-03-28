package io.github.helloworlde.netty.rpc.serialize;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SerializeEnum {
    JSON(1, JsonSerialize.getInstance()),
    ;

    private Integer id;
    private Serialize serialize;

    public static Serialize getById(int id) {
        return Arrays.stream(values())
                     .filter(s -> s.id.equals(id))
                     .findFirst()
                     .map(s -> s.serialize)
                     .orElseThrow(() -> new IllegalArgumentException("Unknown Serialize type"));
    }
}
