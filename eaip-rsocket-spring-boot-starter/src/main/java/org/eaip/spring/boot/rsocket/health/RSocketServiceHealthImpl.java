package org.eaip.spring.boot.rsocket.health;

import org.eaip.rsocket.RSocketService;
import org.eaip.rsocket.health.RSocketServiceHealth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RSocket service health default implementation
 *
 * @author CuiChangHe
 */
@RSocketService(serviceInterface = RSocketServiceHealth.class)
public class RSocketServiceHealthImpl implements RSocketServiceHealth {
    @Autowired
    private List<ReactiveHealthIndicator> healthIndicators;

    @Override
    public Mono<Integer> check(String serviceName) {
        return Flux.fromIterable(healthIndicators)
                .flatMap(healthIndicator -> healthIndicator
                        .health().map(Health::getStatus))
                .all(status -> status == Status.UP)
                .map(result -> result ? SERVING_STATUS : DOWN_STATUS);
    }
}
