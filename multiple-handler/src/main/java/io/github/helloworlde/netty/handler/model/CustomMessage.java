package io.github.helloworlde.netty.handler.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomMessage {
    private Long id;

    private String message;

    private Long timestamp;
}
