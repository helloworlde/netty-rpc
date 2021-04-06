package io.github.helloworlde.netty.rpc.starter.opentelemetry;

import io.github.helloworlde.netty.opentelemetry.trace.config.ExporterType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "netty.rpc.opentelemetry")
public class OpenTelemetryProperties {

    private boolean enabled = true;

    private int sampleRatio = 1;

    @Value("${spring.application.name:application}")
    private String serviceName;

    @NestedConfigurationProperty
    private Exporter exporter = new Exporter();

    @Data
    static class Exporter {

        private ExporterType name;

        private String address;

        private int port;

    }
}
