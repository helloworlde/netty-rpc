package io.github.helloworlde.netty.opentelemetry.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.CollectorRegistry;

public class MetricsConfiguration {

    private final SdkMeterProvider provider;

    private final Meter meter;

    private final CollectorRegistry collectorRegistry;

    private final PrometheusCollector prometheusCollector;

    public MetricsConfiguration(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;

        this.provider = SdkMeterProvider.builder()
                                        .buildAndRegisterGlobal();

        this.meter = provider.get("NETTY_RPC");

        this.prometheusCollector = PrometheusCollector.builder()
                                                      .setMetricProducer(provider)
                                                      .buildAndRegister()
                                                      .register(collectorRegistry);
    }


    public Meter getMeter() {
        return meter;
    }

}
