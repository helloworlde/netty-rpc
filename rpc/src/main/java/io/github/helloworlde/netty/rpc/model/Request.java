package io.github.helloworlde.netty.rpc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Request {

    private Long requestId;

    private Object body;

    private Header header;
}
