package io.github.helloworlde.netty.rpc.starter.opentelemetry;

import io.github.helloworlde.netty.rpc.opentelemetry.trace.config.ExporterType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "netty.rpc.opentelemetry")
public class OpenTelemetryProperties {

    @Value("${spring.application.name:application}")
    private String serviceName;

    @NestedConfigurationProperty
    private Trace trace = new Trace();

    @NestedConfigurationProperty
    private Metrics metrics = new Metrics();


    @Data
    static class Metrics {
        private boolean enabled = true;
    }

    @Data
    static class Trace {
        private boolean enabled = true;

        private int sampleRatio = 1;

        @NestedConfigurationProperty
        private Exporter exporter = new Exporter();
    }

    @Data
    static class Exporter {

        private ExporterType name = ExporterType.logging;

        private String address;

        private int port;

    }
}
