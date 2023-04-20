package org.eaip.rsocket.client;

import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.events.CloudEventsProcessor;
import org.eaip.rsocket.events.ServicesExposedEvent;
import org.eaip.rsocket.events.ServicesHiddenEvent;
import org.eaip.rsocket.health.RSocketServiceHealth;
import org.eaip.rsocket.invocation.RSocketRemoteServiceBuilder;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.eaip.rsocket.rpc.LocalReactiveServiceCaller;
import org.eaip.rsocket.upstream.UpstreamCluster;
import org.eaip.rsocket.upstream.UpstreamClusterChangedEventConsumer;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.eaip.rsocket.upstream.UpstreamManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RSocket Broker Client
 *
 * @author CuiCHangHe
 */
@SuppressWarnings("rawtypes")
public class RSocketBrokerClient {
    private static Logger log = LoggerFactory.getLogger(RSocketBrokerClient.class);
    private List<String> brokers;
    private String appName;
    private RSocketMimeType dataMimeType;
    private UpstreamManager upstreamManager;
    private LocalReactiveServiceCaller serviceCaller;
    private Sinks.Many<CloudEventImpl> eventProcessor;
    private SimpleRSocketRequesterSupport rsocketRequesterSupport;
    private CloudEventsProcessor cloudEventsProcessor;

    public RSocketBrokerClient(String appName, List<String> brokers,
                               String topology, Map<String, String> metadata,
                               RSocketMimeType dataMimeType, char[] jwtToken,
                               LocalReactiveServiceCaller serviceCaller) {
        this.appName = appName;
        this.brokers = brokers;
        this.dataMimeType = dataMimeType;
        this.eventProcessor = Sinks.many().multicast().onBackpressureBuffer();
        this.serviceCaller = serviceCaller;
        // add health check
        this.serviceCaller.addProvider("", RSocketServiceHealth.class.getCanonicalName(), "",
                RSocketServiceHealth.class, (RSocketServiceHealth) serviceName -> Mono.just(1));
        this.rsocketRequesterSupport = new SimpleRSocketRequesterSupport(appName, jwtToken, this.brokers,
                this.serviceCaller, this.eventProcessor);
        this.rsocketRequesterSupport.setTopology(topology);
        this.rsocketRequesterSupport.setMetadata(metadata);
        this.cloudEventsProcessor = new CloudEventsProcessor(eventProcessor, new ArrayList<>());
        initUpstreamManager();
    }

    private void initUpstreamManager() {
        this.upstreamManager = new UpstreamManagerImpl(rsocketRequesterSupport);
        upstreamManager.add(new UpstreamCluster(null, "*", null, this.brokers));
        try {
            upstreamManager.init();
            this.cloudEventsProcessor.addConsumer(new UpstreamClusterChangedEventConsumer(upstreamManager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RSocketBrokerClient addService(String serviceName, Class<?> serviceInterface, Object handler) {
        this.serviceCaller.addProvider("", serviceName, "", serviceInterface, handler);
        return this;
    }

    public void publishServices() {
        CloudEventImpl<ServicesExposedEvent> servicesExposedEventCloudEvent = rsocketRequesterSupport.servicesExposedEvent().get();
        if (servicesExposedEventCloudEvent != null) {
            upstreamManager.findBroker().getLoadBalancedRSocket().fireCloudEventToUpstreamAll(servicesExposedEventCloudEvent).doOnSuccess(aVoid -> {
                String exposedServices = rsocketRequesterSupport.exposedServices().get().stream().map(ServiceLocator::getGsv).collect(Collectors.joining(","));
                log.info(RsocketErrorCode.message("RST-301201", exposedServices, brokers));
            }).subscribe();
        }
    }

    public void removeService(String serviceName, Class<?> serviceInterface) {
        ServiceLocator targetService = new ServiceLocator("", serviceName, "");
        CloudEventImpl<ServicesHiddenEvent> cloudEvent = ServicesHiddenEvent.convertServicesToCloudEvent(Collections.singletonList(targetService));
        upstreamManager.findBroker().getLoadBalancedRSocket().fireCloudEventToUpstreamAll(cloudEvent)
                .doOnSuccess(unused -> {
                    this.serviceCaller.removeProvider("", serviceName, "", serviceInterface);
                }).subscribe();
    }

    public void dispose() {
        this.upstreamManager.close();
    }

    public <T> T buildService(Class<T> serviceInterface) {
        return RSocketRemoteServiceBuilder
                .client(serviceInterface)
                .service(serviceInterface.getCanonicalName())
                .encodingType(this.dataMimeType)
                .acceptEncodingType(this.dataMimeType)
                .upstreamManager(this.upstreamManager)
                .build();
    }

    public <T> T buildService(Class<T> serviceInterface, String serviceName) {
        return RSocketRemoteServiceBuilder
                .client(serviceInterface)
                .service(serviceName)
                .encodingType(this.dataMimeType)
                .acceptEncodingType(this.dataMimeType)
                .upstreamManager(this.upstreamManager)
                .build();
    }

}
