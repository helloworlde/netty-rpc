package io.github.helloworlde.netty.rpc.client.heartbeat;

import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.model.Heartbeat;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.service.HeartbeatService;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class HeartbeatTask {

    private final AtomicLong requestSeq = new AtomicLong();

    private final AtomicInteger failCounter = new AtomicInteger();
    private final ScheduledExecutorService executorService;
    private final Transport transport;
    private final int MAX_FAIL_COUNT = 5;

    public HeartbeatTask(Transport transport) {
        log.info("创建心跳");
        this.transport = transport;
        this.executorService = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("heartbeat"));
        this.executorService.scheduleAtFixedRate(() -> {
            Boolean success = sendHeartbeat();
            if (success) {
                failCounter.set(0);
            } else {
                failCounter.incrementAndGet();
                if (failCounter.get() > MAX_FAIL_COUNT) {
                    log.info("已经心跳达到最大失败次数，关闭 Transport");
                    transport.shutdown();
                }
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    private Boolean sendHeartbeat() {
        ResponseFuture<Object> responseFuture;
        try {
            long sequenceId = requestSeq.getAndIncrement();
            Heartbeat heartbeat = Heartbeat.builder()
                                           .sequenceId(sequenceId)
                                           .timestamp(System.currentTimeMillis())
                                           .build();

            log.info("发送心跳: {}", heartbeat);
            Request request = RequestInvoker.createRequest(HeartbeatService.class, "heartbeat", heartbeat);

            responseFuture = new ResponseFuture<>();
            transport.write(request, responseFuture);

            responseFuture.await(500);
        } catch (Exception e) {
            log.error("心跳失败: {}", e.getMessage(), e);
            return false;
        }
        return responseFuture.isSuccess();
    }

    public void shutdown() {
        log.info("关闭心跳");
        this.executorService.shutdown();
    }

}
