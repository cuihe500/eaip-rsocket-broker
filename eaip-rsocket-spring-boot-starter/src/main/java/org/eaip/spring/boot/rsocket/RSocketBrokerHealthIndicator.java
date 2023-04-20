package org.eaip.spring.boot.rsocket;

import org.eaip.rsocket.events.AppStatusEvent;
import org.eaip.rsocket.health.RSocketServiceHealth;
import org.eaip.rsocket.invocation.RSocketRemoteServiceBuilder;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import reactor.core.publisher.Mono;

/**
 * RSocket Broker health indicator
 *
 * @author CuiChangHe
 */
public class RSocketBrokerHealthIndicator implements ReactiveHealthIndicator {
    private RSocketServiceHealth rsocketServiceHealth;
    private RSocketEndpoint rsocketEndpoint;
    private String brokers;


    public RSocketBrokerHealthIndicator(RSocketEndpoint rsocketEndpoint, UpstreamManager upstreamManager, String brokers) {
        this.rsocketEndpoint = rsocketEndpoint;
        this.rsocketServiceHealth = RSocketRemoteServiceBuilder
                .client(RSocketServiceHealth.class)
                .nativeImage()
                .upstreamManager(upstreamManager)
                .build();
        this.brokers = brokers;
    }

    @Override
    public Mono<Health> health() {
        return rsocketServiceHealth.check(null)
                .map(result -> {
                            boolean brokerAlive = result != null && result == 1;
                            boolean localServicesAlive = !rsocketEndpoint.getRsocketServiceStatus().equals(AppStatusEvent.STATUS_STOPPED);
                            Health.Builder builder = brokerAlive && localServicesAlive ? Health.up() : Health.outOfService();
                            builder.withDetail("brokers", brokers);
                            builder.withDetail("localServiceStatus", AppStatusEvent.statusText(rsocketEndpoint.getRsocketServiceStatus()));
                            return builder.build();
                        }
                )
                .onErrorReturn(Health.down().withDetail("brokers", brokers).build());
    }

}
