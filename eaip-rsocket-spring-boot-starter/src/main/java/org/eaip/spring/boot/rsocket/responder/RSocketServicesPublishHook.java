package org.eaip.spring.boot.rsocket.responder;

import org.eaip.rsocket.RSocketAppContext;
import org.eaip.rsocket.RSocketRequesterSupport;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;
import org.eaip.rsocket.events.AppStatusEvent;
import org.eaip.rsocket.events.PortsUpdateEvent;
import org.eaip.rsocket.events.ServicesExposedEvent;
import org.eaip.rsocket.loadbalance.LoadBalancedRSocket;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.eaip.rsocket.upstream.UpstreamCluster;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.stream.Collectors;

/**
 * RSocket services publish hook
 *
 * @author CuiChangHe
 */
public class RSocketServicesPublishHook implements ApplicationListener<ApplicationReadyEvent> {
    private static Logger log = LoggerFactory.getLogger(RSocketServicesPublishHook.class);
    @Autowired
    private UpstreamManager upstreamManager;
    @Autowired
    private RSocketRequesterSupport rsocketRequesterSupport;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent applicationReadyEvent) {
        UpstreamCluster brokerCluster = upstreamManager.findBroker();
        if (brokerCluster == null) return;
        //rsocket broker cluster logic
        CloudEventImpl<AppStatusEvent> appStatusEventCloudEvent = RSocketCloudEventBuilder
                .builder(new AppStatusEvent(RSocketAppContext.ID, AppStatusEvent.STATUS_SERVING))
                .build();
        LoadBalancedRSocket loadBalancedRSocket = brokerCluster.getLoadBalancedRSocket();
        //ports update
        ConfigurableEnvironment env = applicationReadyEvent.getApplicationContext().getEnvironment();
        int serverPort = Integer.parseInt(env.getProperty("server.port", "0"));
        if (serverPort == 0) {
            if (RSocketAppContext.webPort > 0 || RSocketAppContext.managementPort > 0 || RSocketAppContext.rsocketPorts != null) {
                PortsUpdateEvent portsUpdateEvent = new PortsUpdateEvent();
                portsUpdateEvent.setAppId(RSocketAppContext.ID);
                portsUpdateEvent.setWebPort(RSocketAppContext.webPort);
                portsUpdateEvent.setManagementPort(RSocketAppContext.managementPort);
                portsUpdateEvent.setRsocketPorts(RSocketAppContext.rsocketPorts);
                CloudEventImpl<PortsUpdateEvent> portsUpdateCloudEvent = RSocketCloudEventBuilder
                        .builder(portsUpdateEvent)
                        .build();
                loadBalancedRSocket.fireCloudEventToUpstreamAll(portsUpdateCloudEvent)
                        .doOnSuccess(aVoid -> log.info(RsocketErrorCode.message("RST-301200", loadBalancedRSocket.getActiveUris())))
                        .subscribe();
            }
        }
        // app status
        loadBalancedRSocket.fireCloudEventToUpstreamAll(appStatusEventCloudEvent)
                .doOnSuccess(aVoid -> log.info(RsocketErrorCode.message("RST-301200", loadBalancedRSocket.getActiveUris())))
                .subscribe();
        // service exposed
        CloudEventImpl<ServicesExposedEvent> servicesExposedEventCloudEvent = rsocketRequesterSupport.servicesExposedEvent().get();
        if (servicesExposedEventCloudEvent != null) {
            loadBalancedRSocket.fireCloudEventToUpstreamAll(servicesExposedEventCloudEvent).doOnSuccess(aVoid -> {
                String exposedServices = rsocketRequesterSupport.exposedServices().get().stream().map(ServiceLocator::getGsv).collect(Collectors.joining(","));
                log.info(RsocketErrorCode.message("RST-301201", exposedServices, loadBalancedRSocket.getActiveUris()));
            }).subscribe();
        }
    }
}
