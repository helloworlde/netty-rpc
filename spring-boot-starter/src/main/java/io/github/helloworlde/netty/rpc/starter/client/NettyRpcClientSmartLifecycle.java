package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class NettyRpcClientSmartLifecycle implements SmartLifecycle {


    private final ApplicationContext context;

    private Map<String, Client> clients;

    public NettyRpcClientSmartLifecycle(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void start() {
        log.info("开始启动 Client");
        this.clients = context.getBeansOfType(Client.class);

        clients.forEach((name, client) -> {
            try {
                client.start();
            } catch (Exception e) {
                log.error("启动 Client: {} 失败", name);
            }
        });
    }

    @Override
    public void stop() {
        this.clients.forEach((name, client) -> client.shutdown());
    }

    @Override
    public boolean isRunning() {
        return Objects.nonNull(clients);
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
