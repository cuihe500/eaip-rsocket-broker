package org.eaip.rsocket.observability;

import reactor.core.publisher.Mono;

/**
 * Metrics service for scrape
 *
 * @author CuiCHangHe
 */
public interface MetricsService {
    Mono<String> scrape();
}
