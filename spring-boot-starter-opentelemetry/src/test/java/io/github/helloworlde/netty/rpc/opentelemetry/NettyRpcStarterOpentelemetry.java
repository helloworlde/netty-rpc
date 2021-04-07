package io.github.helloworlde.netty.rpc.opentelemetry;

import io.github.helloworlde.netty.rpc.starter.opentelemetry.OpentelemetryAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ImportAutoConfiguration(OpentelemetryAutoConfiguration.class)
public class NettyRpcStarterOpentelemetry {

    public static void main(String[] args) {
        SpringApplication.run(NettyRpcStarterOpentelemetry.class);
    }

}
