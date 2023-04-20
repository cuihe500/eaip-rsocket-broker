package org.eaip.rsocket.upstream;

import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.events.CloudEventSupport;
import org.eaip.rsocket.events.CloudEventsConsumer;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * UpstreamClusterChangedEvent Consumer to respond the cluster changed
 *
 * @author CuiCHangHe
 */
public class UpstreamClusterChangedEventConsumer implements CloudEventsConsumer {
    private static Logger log = LoggerFactory.getLogger(UpstreamClusterChangedEventConsumer.class);
    private UpstreamManager upstreamManager;

    public UpstreamClusterChangedEventConsumer(UpstreamManager upstreamManager) {
        this.upstreamManager = upstreamManager;
    }

    @Override
    public boolean shouldAccept(CloudEventImpl<?> cloudEvent) {
        String type = cloudEvent.getAttributes().getType();
        String sourcing = cloudEvent.getSourcing();
        return UpstreamClusterChangedEvent.class.getCanonicalName().equalsIgnoreCase(type)
                && sourcing != null && sourcing.startsWith("upstream:broker:");
    }

    @Override
    public Mono<Void> accept(CloudEventImpl<?> cloudEvent) {
        return Mono.fromRunnable(() -> {
            handleUpstreamClusterChangedEvent(cloudEvent);
        });
    }

    public void handleUpstreamClusterChangedEvent(CloudEventImpl<?> cloudEvent) {
        UpstreamClusterChangedEvent clusterChangedEvent = CloudEventSupport.unwrapData(cloudEvent, UpstreamClusterChangedEvent.class);
        if (clusterChangedEvent != null) {
            String serviceId = ServiceLocator.serviceId(clusterChangedEvent.getGroup(), clusterChangedEvent.getInterfaceName(), clusterChangedEvent.getVersion());
            UpstreamCluster upstreamCluster = upstreamManager.findClusterByServiceId(serviceId);
            if (upstreamCluster != null) {
                upstreamCluster.setUris(clusterChangedEvent.getUris());
                log.info(RsocketErrorCode.message("RST-300202", serviceId, String.join(",", clusterChangedEvent.getUris())));
            }
        }
    }
}
