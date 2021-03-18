package io.github.helloworlde.netty.rpc.model;

import io.github.helloworlde.netty.rpc.error.RpcException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Response {

    private Long requestId;

    private Object body;

    private Map<String, Object> extra;

    private RpcException exception;

}
