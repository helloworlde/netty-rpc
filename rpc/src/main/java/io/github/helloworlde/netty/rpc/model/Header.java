package io.github.helloworlde.netty.rpc.model;

import lombok.Data;

import java.util.Map;

@Data
public class Header {

    private String className;

    private String methodName;

    private Map<String, Object> extraHeader;
}
