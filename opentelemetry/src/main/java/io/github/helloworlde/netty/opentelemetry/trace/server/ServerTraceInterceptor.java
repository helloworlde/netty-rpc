package io.github.helloworlde.netty.opentelemetry.trace.server;

import io.github.helloworlde.netty.opentelemetry.trace.config.TracerTextMapGetter;
import io.github.helloworlde.netty.rpc.interceptor.Metadata;
import io.github.helloworlde.netty.rpc.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.interceptor.ServerInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ServerTraceInterceptor implements ServerInterceptor {


    private OpenTelemetry openTelemetry;

    private Tracer tracer;

    private TextMapPropagator textFormat;

    private TextMapGetter<Map<String, Object>> getter;

    public ServerTraceInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("NETTY_RPC");
        this.textFormat = openTelemetry.getPropagators().getTextMapPropagator();
        this.getter = new TracerTextMapGetter();
    }

    @Override
    public Object interceptorCall(Request request, Metadata metadata, ServerCall next) throws Exception {
        // 从请求中获取 Trace 上下文
        Context extractContext = textFormat.extract(Context.current(), request.getExtra(), getter);

        // 初始化 Span
        String spanName = String.format("%s#%s", request.getServiceName(), request.getMethodName());
        Span span = tracer.spanBuilder(spanName)
                          .setAttribute("ServiceName", request.getServiceName())
                          .setAttribute("MethodName", request.getMethodName())
                          .setAttribute("RequestId", request.getRequestId())
                          // 将传递的上下文信息添加到当前的 Span 中
                          .setParent(extractContext)
                          .setSpanKind(SpanKind.SERVER)
                          .startSpan();

        span.addEvent("开始处理");

        metadata.getAttributes()
                .forEach((key, value) -> span.setAttribute(key, String.valueOf(value)));

        Object result = null;
        // 保存并创建新的 Trace 上下文
        try (Scope ignored = span.makeCurrent()) {
            result = next.call(request, metadata);
            span.addEvent("处理结束");
        } catch (Exception e) {
            span.addEvent("处理异常");
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            log.info("服务端 TraceId: {}", span.getSpanContext().getTraceId());
            span.setStatus(StatusCode.OK);
            span.end();
        }

        return result;
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 1;
    }

}
