package org.eaip.rsocket.client;

import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.util.ByteBufPayload;
import org.eaip.rsocket.RSocketAppContext;
import org.eaip.rsocket.RSocketRequesterSupport;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.events.ServicesExposedEvent;
import org.eaip.rsocket.health.RSocketServiceHealth;
import org.eaip.rsocket.metadata.AppMetadata;
import org.eaip.rsocket.metadata.BearerTokenMetadata;
import org.eaip.rsocket.metadata.RSocketCompositeMetadata;
import org.eaip.rsocket.metadata.ServiceRegistryMetadata;
import org.eaip.rsocket.observability.MetricsService;
import org.eaip.rsocket.rpc.LocalReactiveServiceCaller;
import org.eaip.rsocket.rpc.RSocketResponderHandler;
import org.eaip.rsocket.rpc.ReactiveServiceDiscovery;
import org.eaip.rsocket.transport.NetworkUtil;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Simple RSocketRequesterSupport for App
 *
 * @author CuiCHangHe
 */
@SuppressWarnings("rawtypes")
public class SimpleRSocketRequesterSupport implements RSocketRequesterSupport {
    private char[] jwtToken;
    private List<String> brokers;
    private String appName;
    private String topology = "intranet";
    private Map<String, String> metadata;
    private LocalReactiveServiceCaller serviceCaller;
    private Sinks.Many<CloudEventImpl> eventProcessor;

    public SimpleRSocketRequesterSupport(String appName, char[] jwtToken, List<String> brokers,
                                         LocalReactiveServiceCaller serviceCaller,
                                         Sinks.Many<CloudEventImpl> eventProcessor) {
        this.appName = appName;
        this.jwtToken = jwtToken;
        this.brokers = brokers;
        this.eventProcessor = eventProcessor;
        this.serviceCaller = serviceCaller;
    }

    public void setTopology(String topology) {
        this.topology = topology;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public URI originUri() {
        return URI.create("tcp://" + NetworkUtil.LOCAL_IP + "?appName=" + appName + "&uuid=" + RSocketAppContext.ID);
    }

    @Override
    public Supplier<Payload> setupPayload(String serviceId) {
        return () -> {
            //composite metadata with app metadata
            RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(getAppMetadata());
            //authentication for broker
            if (serviceId.equals("*")) {
                if (this.jwtToken != null && this.jwtToken.length > 0) {
                    compositeMetadata.addMetadata(new BearerTokenMetadata(this.jwtToken));
                }
            }
            Set<ServiceLocator> serviceLocators = exposedServices().get();
            if (!serviceLocators.isEmpty()) {
                ServiceRegistryMetadata serviceRegistryMetadata = new ServiceRegistryMetadata();
                serviceRegistryMetadata.setPublished(serviceLocators);
                compositeMetadata.addMetadata(serviceRegistryMetadata);
            }
            return ByteBufPayload.create(Unpooled.EMPTY_BUFFER, compositeMetadata.getContent());
        };
    }

    @Override
    public Supplier<Set<ServiceLocator>> exposedServices() {
        Set<String> allServices = this.serviceCaller.findAllServices();
        if (!allServices.isEmpty()) {
            return () -> allServices.stream()
                    .filter(serviceName -> !serviceName.equals(ReactiveServiceDiscovery.class.getCanonicalName())
                            && !serviceName.equals(RSocketServiceHealth.class.getCanonicalName())
                            && !serviceName.equals(MetricsService.class.getCanonicalName()))
                    .map(serviceName -> new ServiceLocator("", serviceName, ""))
                    .collect(Collectors.toSet());
        }
        return Collections::emptySet;
    }

    @Override
    public Supplier<Set<ServiceLocator>> subscribedServices() {
        return Collections::emptySet;
    }

    @Override
    public Supplier<CloudEventImpl<ServicesExposedEvent>> servicesExposedEvent() {
        return () -> {
            Collection<ServiceLocator> serviceLocators = exposedServices().get();
            if (serviceLocators.isEmpty()) return null;
            return ServicesExposedEvent.convertServicesToCloudEvent(serviceLocators);
        };
    }

    @Override
    public SocketAcceptor socketAcceptor() {
        return (setupPayload, requester) -> Mono.fromCallable(() -> new RSocketResponderHandler(serviceCaller, eventProcessor, requester, setupPayload));
    }

    @Override
    public List<RSocketInterceptor> responderInterceptors() {
        return Collections.emptyList();
    }

    @Override
    public List<RSocketInterceptor> requestInterceptors() {
        return Collections.emptyList();
    }

    private AppMetadata getAppMetadata() {
        //app metadata
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.setUuid(RSocketAppContext.ID);
        appMetadata.setName(appName);
        appMetadata.setIp(NetworkUtil.LOCAL_IP);
        appMetadata.setDevice(appName);
        appMetadata.setBrokers(brokers);
        appMetadata.setTopology(this.topology);
        appMetadata.setMetadata(this.metadata);
        return appMetadata;
    }
}
