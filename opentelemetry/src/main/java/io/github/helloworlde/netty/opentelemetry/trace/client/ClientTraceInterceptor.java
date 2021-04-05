package io.github.helloworlde.netty.opentelemetry.trace.client;

import io.github.helloworlde.netty.opentelemetry.trace.config.TracerTextMapSetter;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class ClientTraceInterceptor implements ClientInterceptor {

    private OpenTelemetry openTelemetry;

    private Tracer tracer;

    private TextMapPropagator textFormat;

    private final TextMapSetter<Map<String, Object>> setter;

    public ClientTraceInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("NETTY_RPC");
        this.textFormat = openTelemetry.getPropagators().getTextMapPropagator();
        this.setter = new TracerTextMapSetter();
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        String spanName = String.format("%s#%s", request.getServiceName(), request.getMethodName());
        Span span = tracer.spanBuilder(spanName)
                          .setNoParent()
                          .setSpanKind(SpanKind.CLIENT)
                          .setAttribute("ServiceName", request.getServiceName())
                          .setAttribute("MethodName", request.getMethodName())
                          .setAttribute("RequestId", request.getRequestId())
                          .setAttribute("Timeout", callOptions.getTimeout())
                          .setStartTimestamp(Instant.now())
                          .startSpan();

        Object result;


        try (Scope ignored = span.makeCurrent()) {
            textFormat.inject(Context.current(), callOptions.getAttributes(), setter);
            span.addEvent("开始调用");
            result = next.call(request, callOptions);
            span.addEvent("接收到结果");
        } catch (Exception e) {
            span.addEvent("调用异常");
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            log.info("客户端 TraceId: {}", span.getSpanContext().getTraceId());
            span.end();
        }
        return result;
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE + 1;
    }
}
