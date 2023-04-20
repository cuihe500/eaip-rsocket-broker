package org.eaip.rsocket.broker.filters;

import org.eaip.rsocket.RSocketExchange;
import org.eaip.rsocket.ServiceLocator;
import org.eaip.rsocket.metadata.GSVRoutingMetadata;
import org.eaip.rsocket.route.RSocketFilter;
import org.eaip.spring.boot.rsocket.broker.route.ServiceRoutingSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Canary filter for services(Example only)
 *
 * @author CuiChangHe
 */
@Component
@ConditionalOnExpression(value = "false")
public class CanaryFilter extends RSocketFilter {
    private static String canaryVersion = "canary";
    private List<String> canaryServices = Arrays.asList("org.eaip.Service1", "org.eaip.Service2");
    private int roundRobinIndex = 0;
    private int trafficRating = 30;

    @Autowired
    private ServiceRoutingSelector routingSelector;

    @Override
    public String name() {
        return "RSocket Canary filter -- " + trafficRating + "% traffic to canary version";
    }

    @Override
    public Mono<Boolean> shouldFilter(RSocketExchange exchange) {
        GSVRoutingMetadata routingMetadata = exchange.getRoutingMetadata();
        if (canaryServices.contains(routingMetadata.getService())) {
            String canaryRouting = ServiceLocator.serviceId(routingMetadata.getGroup(), routingMetadata.getService(), canaryVersion);
            if (routingSelector.findHandler(ServiceLocator.serviceHashCode(canaryRouting)) != null) {
                return Mono.just(true);
            }
        }
        return Mono.just(false);
    }

    @Override
    public Mono<Void> run(RSocketExchange exchange) {
        if (roundRobinIndex % 100 < trafficRating) {
            GSVRoutingMetadata routingMetadata = exchange.getRoutingMetadata();
            routingMetadata.setVersion(canaryVersion);
        }
        roundRobinIndex = (roundRobinIndex + 1) % 100;
        return Mono.empty();
    }
}
