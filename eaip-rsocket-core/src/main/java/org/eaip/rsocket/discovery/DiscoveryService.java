package org.eaip.rsocket.discovery;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * discovery service for registry client
 *
 * @author CuiCHangHe
 */
public interface DiscoveryService {

    Mono<List<RSocketServiceInstance>> getInstances(String serviceId);

    Mono<RSocketServiceInstance> getInstance(String appId);

    Mono<List<String>> findAppInstances(String orgId);

    Mono<List<String>> getAllServices();
}
