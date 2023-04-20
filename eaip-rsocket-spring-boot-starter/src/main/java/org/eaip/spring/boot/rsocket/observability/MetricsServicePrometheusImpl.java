package org.eaip.spring.boot.rsocket.observability;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.eaip.rsocket.RSocketService;
import org.eaip.rsocket.observability.MetricsService;
import reactor.core.publisher.Mono;

/**
 * metrics service Prometheus implementation
 *
 * @author CuiChangHe
 */
@RSocketService(serviceInterface = MetricsService.class)
public class MetricsServicePrometheusImpl implements MetricsService {
    private PrometheusMeterRegistry meterRegistry;

    public MetricsServicePrometheusImpl(PrometheusMeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<String> scrape() {
        return Mono.fromCallable(() -> meterRegistry.scrape());
    }
}
