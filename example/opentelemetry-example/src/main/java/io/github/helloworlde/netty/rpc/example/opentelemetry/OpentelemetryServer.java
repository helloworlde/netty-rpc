package io.github.helloworlde.netty.rpc.example.opentelemetry;

import io.github.helloworlde.netty.opentelemetry.ExporterEnum;
import io.github.helloworlde.netty.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.opentelemetry.ServerTraceInterceptor;
import io.github.helloworlde.netty.rpc.example.opentelemetry.service.impl.HelloServiceImpl;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.ServerBuilder;
import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpentelemetryServer {

    public static void main(String[] args) throws InterruptedException {
        OpenTelemetry telemetry = OpenTelemetryConfig.getOpenTelemetry(ExporterEnum.Jaeger, "服务端", "127.0.0.1", 14250);
        // OpenTelemetry telemetry = OpenTelemetryConfig.getOpenTelemetry(ExporterEnum.Zipkin, "服务端", "127.0.0.1", 9411);

        Server server = ServerBuilder.builder()
                                     .port(9096)
                                     .addService(HelloService.class, new HelloServiceImpl())
                                     .addInterceptor(new ServerTraceInterceptor(telemetry))
                                     .build();

        server.start();
        server.awaitTermination();
    }
}
