package io.github.helloworlde.netty.rpc.opentelemetry.trace.client;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.opentelemetry.trace.config.TracerTextMapSetter;
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
        // 通过 OpenTelemetry 获取 Tracer
        this.tracer = openTelemetry.getTracer("NETTY_RPC");
        // 获取上下文传输对象
        this.textFormat = openTelemetry.getPropagators().getTextMapPropagator();
        // 初始化上下文传输设置
        this.setter = new TracerTextMapSetter();
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        // 创建 Span
        String spanName = String.format("%s#%s", request.getServiceName(), request.getMethodName());
        Span span = tracer.spanBuilder(spanName)
                          .setSpanKind(SpanKind.CLIENT)
                          .setAttribute("ServiceName", request.getServiceName())
                          .setAttribute("MethodName", request.getMethodName())
                          .setAttribute("RequestId", request.getRequestId())
                          .setAttribute("Timeout", callOptions.getTimeout())
                          .setStartTimestamp(Instant.now())
                          .startSpan();

        Object result;

        // 保存并创建新的 Trace 上下文
        try (Scope ignored = span.makeCurrent()) {
            // 将当前的上下文添加到传播中，用于传递给下游
            textFormat.inject(Context.current(), callOptions.getAttributes(), setter);
            span.addEvent("开始调用");
            // 调用下游
            result = next.call(request, callOptions);
            span.addEvent("接收到结果");
        } catch (Exception e) {
            span.addEvent("调用异常");
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            log.debug("客户端 TraceId: {}", span.getSpanContext().getTraceId());
            span.setStatus(StatusCode.OK);
            // 结束 Span
            span.end();
        }
        return result;
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE + 1;
    }
}
