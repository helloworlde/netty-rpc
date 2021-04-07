package io.github.helloworlde.netty.opentelemetry.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.CollectorRegistry;

public class MetricsConfiguration {

    private final SdkMeterProvider provider;

    private final Meter meter;

    private final PrometheusCollector prometheusCollector;

    public MetricsConfiguration(CollectorRegistry collectorRegistry) {
        // 注册提供数据的对象
        this.provider = SdkMeterProvider.builder()
                                        .buildAndRegisterGlobal();

        // 注册 Prometheus 数据收集对象
        this.prometheusCollector = PrometheusCollector.builder()
                                                      .setMetricProducer(provider)
                                                      .buildAndRegister();

        // 将 Prometheus 数据收集对象注册到收集器中，用于 Spring Boot Actuator 对外提供
        collectorRegistry.register(prometheusCollector);
        // 初始化数据测量
        this.meter = provider.get("NETTY_RPC");
    }


    public Meter getMeter() {
        return meter;
    }

    public SdkMeterProvider getProvider() {
        return provider;
    }

    public PrometheusCollector getPrometheusCollector() {
        return prometheusCollector;
    }
}
