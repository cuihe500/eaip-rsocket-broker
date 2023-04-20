package org.eaip.rsocket.observability;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Tracing Service
 *
 * @author CuiCHangHe
 */
public interface TracingService {

    Mono<Void> sendSpans(List<byte[]> encodedSpans);
}
