package io.github.helloworlde.netty.rpc.client.interceptor;

import io.github.helloworlde.netty.rpc.client.transport.Transport;

import java.util.HashMap;
import java.util.Map;

public class CallOptions {

    private Map<String, Object> attributes = new HashMap<>();

    private Long timeout = 0L;

    private Transport transport;

    public CallOptions withAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public CallOptions withTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

    public CallOptions withTransport(Transport transport) {
        this.transport = transport;
        return this;
    }

    public Transport getTransport() {
        return this.transport;
    }
}
