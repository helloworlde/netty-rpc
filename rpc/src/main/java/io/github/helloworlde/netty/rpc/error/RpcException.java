package io.github.helloworlde.netty.rpc.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcException extends Exception {


    private Long requestId;
    private String message;
    private Throwable throwable;

    public RpcException(String message) {
        this.message = message;
    }

    public RpcException(Long requestId, String message) {
        this.requestId = requestId;
        this.message = message;
    }

    public RpcException(Long requestId, Throwable cause) {
        super(cause);
        this.requestId = requestId;
        this.message = cause.getMessage();
    }
}
