package io.github.helloworlde.netty.rpc.interceptor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class CallOptions {

    private Map<String, Object> attributes;

    private Long timeout = 10_000L;

    private LocalDateTime deadline;

    public CallOptions() {
        this.attributes = new HashMap<>();
        this.deadline = LocalDateTime.now().plus(this.timeout, ChronoUnit.MILLIS);
    }

    public boolean checkDeadlineExceeded() throws TimeoutException {
        boolean expired = Objects.nonNull(deadline) && LocalDateTime.now().isAfter(deadline);
        if (expired) {
            throw new TimeoutException("请求已经超时");
        }
        return false;
    }


    public CallOptions withAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public CallOptions withTimeout(Long timeout) {
        this.timeout = timeout;
        deadline = LocalDateTime.now().plus(timeout, ChronoUnit.MILLIS);
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

}
