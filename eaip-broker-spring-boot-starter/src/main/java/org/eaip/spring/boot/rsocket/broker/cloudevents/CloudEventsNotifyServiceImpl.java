package org.eaip.spring.boot.rsocket.broker.cloudevents;

import org.eaip.rsocket.cloudevents.CloudEventsNotifyService;
import org.eaip.spring.boot.rsocket.broker.responder.RSocketBrokerHandlerRegistry;
import org.eaip.spring.boot.rsocket.broker.responder.RSocketBrokerResponderHandler;
import org.eaip.spring.boot.rsocket.broker.supporting.RSocketLocalService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CloudEvents notify service implementation
 *
 * @author CuiChangHe
 */
@RSocketLocalService(serviceInterface = CloudEventsNotifyService.class)
public class CloudEventsNotifyServiceImpl implements CloudEventsNotifyService {
    private RSocketBrokerHandlerRegistry handlerRegistry;

    public CloudEventsNotifyServiceImpl(RSocketBrokerHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    @Override
    public Mono<Void> notify(String appId, String cloudEventJson) {
        RSocketBrokerResponderHandler handler = handlerRegistry.findByUUID(appId);
        if (handler != null) {
            return handler.fireCloudEventToPeer(cloudEventJson);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> notifyAll(String appName, String cloudEventJson) {
        return Flux.fromIterable(handlerRegistry.findByAppName(appName))
                .flatMap(responderHandler -> responderHandler.fireCloudEventToPeer(cloudEventJson))
                .then();
    }
}
