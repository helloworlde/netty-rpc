package io.github.helloworlde.netty.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private Long requestId;

    private Object body;

    private String message;

    private Status status;

    private Map<String, Object> header;
}
