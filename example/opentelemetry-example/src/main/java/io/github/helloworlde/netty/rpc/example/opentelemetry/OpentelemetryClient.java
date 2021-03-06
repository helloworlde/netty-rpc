package io.github.helloworlde.netty.rpc.example.opentelemetry;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.ClientBuilder;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import io.github.helloworlde.netty.rpc.example.service.HelloService;
import io.github.helloworlde.netty.rpc.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.rpc.opentelemetry.trace.client.ClientTraceInterceptor;
import io.github.helloworlde.netty.rpc.opentelemetry.trace.config.ExporterType;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpentelemetryClient {

    public static void main(String[] args) throws Exception {

        OpenTelemetry telemetry = OpenTelemetryConfig.getOpenTelemetry(ExporterType.jaeger, "客户端", "127.0.0.1", 14250);
        // OpenTelemetry telemetry = OpenTelemetryConfig.getOpenTelemetry(ExporterEnum.Zipkin, "客户端", "127.0.0.1", 9411);
        final Span span = telemetry.getTracer("NETTY_RPC")
                                   .spanBuilder("ClientStart")
                                   .setNoParent()
                                   .startSpan();

        Client client = null;
        try (final Scope scope = span.makeCurrent()) {
            client = ClientBuilder.builder()
                                  .forAddress("127.0.0.1", 9096)
                                  .addInterceptor(new ClientTraceInterceptor(telemetry))
                                  .build();

            client.start();


            HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);

            for (int i = 0; i < 10; i++) {

                String response = helloService.sayHello("Netty RPC " + i);
                log.info("返回的响应结果: {}", response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            span.end();
        }

        client.shutdown();
    }
}
