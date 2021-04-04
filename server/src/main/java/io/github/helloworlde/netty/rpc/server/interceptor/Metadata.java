package io.github.helloworlde.netty.rpc.server.interceptor;

import java.util.HashMap;
import java.util.Map;

public class Metadata {

    private Map<String, Object> attributes = new HashMap<>();

    private Long deadline = 0L;

    public Metadata withAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Metadata withDeadline(Long deadline) {
        this.deadline = deadline;
        return this;
    }

    public Long getDeadline() {
        return deadline;
    }
}
