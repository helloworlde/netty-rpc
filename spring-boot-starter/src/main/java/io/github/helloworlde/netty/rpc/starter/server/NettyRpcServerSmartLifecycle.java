package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.server.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.Objects;

@Slf4j
public class NettyRpcServerSmartLifecycle implements SmartLifecycle {

    private final NettyRpcServiceFactory factory;

    private Server server;

    public NettyRpcServerSmartLifecycle(NettyRpcServiceFactory factory) {
        log.info("初始化 Factory");
        this.factory = factory;
    }

    @Override
    public void start() {
        log.info("服务端开始启动");
        this.server = this.factory.createServer();
    }

    @Override
    public void stop() {
        this.server.shutdown();
    }

    @Override
    public boolean isRunning() {
        return Objects.nonNull(server);
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
