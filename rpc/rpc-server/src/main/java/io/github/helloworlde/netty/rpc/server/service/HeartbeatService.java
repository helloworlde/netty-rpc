package io.github.helloworlde.netty.rpc.server.service;

import io.github.helloworlde.netty.rpc.model.Heartbeat;

public interface HeartbeatService {

    Heartbeat heartbeat(Heartbeat request);

}
