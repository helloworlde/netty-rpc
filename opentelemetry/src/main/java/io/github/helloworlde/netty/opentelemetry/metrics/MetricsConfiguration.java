package io.github.helloworlde.netty.opentelemetry.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.prometheus.client.CollectorRegistry;

import java.util.Arrays;
import java.util.List;

public class MetricsConfiguration {

    private final SdkMeterProvider provider;

    private final Meter meter;

    private final CollectorRegistry collectorRegistry;

    private final PrometheusCollector prometheusCollector;

    private AggregatorFactory aggregatorFactory;

    public MetricsConfiguration(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;

        this.provider = SdkMeterProvider.builder()
                                        .buildAndRegisterGlobal();

        InstrumentSelector selector = InstrumentSelector.builder()
                                                        .setInstrumentType(InstrumentType.COUNTER)
                                                        .build();
        // AggregatorFactory aggregatorFactory = AggregatorFactory.count(AggregationTemporality.CUMULATIVE);
        // AggregatorFactory aggregatorFactory = AggregatorFactory.minMaxSumCount();

        // Double[] boundaries = {0.1d, 0.5d, 0.9d, 0.99d, 1d};
        List<Double> boundaries = Arrays.asList(10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d, 500d, 1000d);

        AggregatorFactory aggregatorFactory = AggregatorFactory.histogram(boundaries, AggregationTemporality.DELTA);

        // this.provider.registerView(selector, aggregatorFactory);

        this.aggregatorFactory = aggregatorFactory;

        this.meter = provider.get("NETTY_RPC");

        this.prometheusCollector = PrometheusCollector.builder()
                                                      .setMetricProducer(provider)
                                                      .buildAndRegister()
                                                      .register(collectorRegistry);
    }


    public Meter getMeter() {
        return meter;
    }


    public AggregatorFactory getAggregatorFactory() {
        return aggregatorFactory;
    }
}
