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

    private String message;

    private Throwable cause;

    public RpcException(String message) {
        super(message);
        this.message = message;
    }

    public RpcException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }

}
