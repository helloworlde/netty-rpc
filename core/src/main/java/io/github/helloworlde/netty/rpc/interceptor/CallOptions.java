package io.github.helloworlde.netty.rpc.interceptor;

import io.github.helloworlde.netty.rpc.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Slf4j
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
            Long timeout = Context.current().getTimeout();
            LocalDateTime startTime = (LocalDateTime) Context.current().getAttribute("startTime");
            long millis = Duration.between(startTime, LocalDateTime.now())
                                  .toMillis();

            log.info("期望超时时间: {}, 实际超时时间: {}", timeout, millis);
            throw new TimeoutException(String.format("请求已经超时，超时时间为: %d ms", millis));
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
        Context.current().withTimeout(timeout);
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

}
