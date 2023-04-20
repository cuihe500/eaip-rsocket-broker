package org.eaip.spring.boot.rsocket.broker.cluster;

import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * RSocket Broker Manager
 *
 * @author CuiChangHe
 */
public interface RSocketBrokerManager {

    Flux<Collection<RSocketBroker>> requestAll();

    RSocketBroker localBroker();

    Collection<RSocketBroker> currentBrokers();

    Mono<RSocketBroker> findByIp(String ip);

    Flux<ServiceLocator> findServices(String ip);

    Boolean isStandAlone();

    String getName();

    void stopLocalBroker();

    Mono<String> broadcast(CloudEventImpl<?> cloudEvent);

    RSocketBroker findConsistentBroker(String clientId);
}
