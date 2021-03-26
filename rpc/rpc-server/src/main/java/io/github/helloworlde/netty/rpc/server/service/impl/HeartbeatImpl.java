package io.github.helloworlde.netty.rpc.server.service.impl;

import io.github.helloworlde.netty.rpc.model.Heartbeat;
import io.github.helloworlde.netty.rpc.server.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatImpl implements HeartbeatService {

    @Override
    public Heartbeat heartbeat(Heartbeat request) {
        log.info("心跳: {}", request);
        return Heartbeat.builder()
                        .sequenceId(request.getSequenceId())
                        .timestamp(System.currentTimeMillis())
                        .build();
    }
}
