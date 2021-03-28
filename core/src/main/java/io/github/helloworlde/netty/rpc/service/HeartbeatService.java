package io.github.helloworlde.netty.rpc.service;

import io.github.helloworlde.netty.rpc.model.Heartbeat;

public interface HeartbeatService {

    Heartbeat heartbeat(Heartbeat request);

}
