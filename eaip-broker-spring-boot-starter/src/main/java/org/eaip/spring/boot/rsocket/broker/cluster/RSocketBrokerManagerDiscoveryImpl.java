package org.eaip.spring.boot.rsocket.broker.cluster;

import io.scalecube.cluster.ClusterMessageHandler;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.eaip.rsocket.transport.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RSocket Broker Manager Reactive Discovery client implementation
 *
 * @author CuiChangHe
 */
public class RSocketBrokerManagerDiscoveryImpl implements RSocketBrokerManager, ClusterMessageHandler, DisposableBean {
    private static final int REFRESH_INTERVAL_SECONDS = 5;
    private static Logger log = LoggerFactory.getLogger(RSocketBrokerManagerDiscoveryImpl.class);
    private final String SERVICE_NAME = "rsocket-broker";
    private ReactiveDiscoveryClient discoveryClient;
    private Map<String, RSocketBroker> currentBrokers = new HashMap<>();
    private Sinks.Many<Collection<RSocketBroker>> brokersEmitterProcessor = Sinks.many().multicast().onBackpressureBuffer();
    private Disposable brokersFresher;

    public RSocketBrokerManagerDiscoveryImpl(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.brokersFresher = Flux.interval(Duration.ofSeconds(REFRESH_INTERVAL_SECONDS))
                .flatMap(aLong -> this.discoveryClient.getInstances(SERVICE_NAME).collectList())
                .subscribe(serviceInstances -> {
                    boolean changed = serviceInstances.size() != currentBrokers.size();
                    for (ServiceInstance serviceInstance : serviceInstances) {
                        if (!currentBrokers.containsKey(serviceInstance.getHost())) {
                            changed = true;
                        }
                    }
                    if (changed) {
                        currentBrokers = serviceInstances.stream().map(serviceInstance -> {
                            RSocketBroker broker = new RSocketBroker();
                            broker.setIp(serviceInstance.getHost());
                            return broker;
                        }).collect(Collectors.toMap(RSocketBroker::getIp, Function.identity()));
                        log.info(RsocketErrorCode.message("RST-300206", String.join(",", currentBrokers.keySet())));
                        brokersEmitterProcessor.tryEmitNext(currentBrokers.values());
                    }
                });
    }

    @Override
    public Flux<Collection<RSocketBroker>> requestAll() {
        return brokersEmitterProcessor.asFlux();
    }

    @Override
    public RSocketBroker localBroker() {
        return currentBrokers.get(NetworkUtil.LOCAL_IP);
    }

    @Override
    public Collection<RSocketBroker> currentBrokers() {
        return currentBrokers.values();
    }

    @Override
    public Mono<RSocketBroker> findByIp(String ip) {
        if (currentBrokers.containsKey(ip)) {
            return Mono.empty();
        } else {
            return Mono.just(currentBrokers.get(ip));
        }
    }

    @Override
    public Flux<ServiceLocator> findServices(String ip) {
        return Flux.empty();
    }

    @Override
    public String getName() {
        return "discovery";
    }

    @Override
    public Boolean isStandAlone() {
        return false;
    }

    @Override
    public void stopLocalBroker() {
        this.brokersFresher.dispose();
    }

    @Override
    public Mono<String> broadcast(CloudEventImpl<?> cloudEvent) {
        return Mono.empty();
    }

    @Override
    public RSocketBroker findConsistentBroker(String clientId) {
        return null;
    }

    @Override
    public void destroy() throws Exception {

    }
}
