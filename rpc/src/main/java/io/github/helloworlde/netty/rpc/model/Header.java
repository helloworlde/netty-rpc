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
public class Header {

    private String serviceName;

    private String methodName;

    private Map<String, Object> extra;
}
