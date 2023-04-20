package org.eaip.spring.boot.rsocket;

import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.events.CloudEventsConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

/**
 * CloudEvent to @EventListener handler
 *
 * @author CuiChangHe
 */
public class CloudEventToListenerConsumer implements CloudEventsConsumer {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public boolean shouldAccept(CloudEventImpl<?> cloudEvent) {
        return true;
    }

    @Override
    public Mono<Void> accept(CloudEventImpl<?> cloudEvent) {
        return Mono.fromRunnable(() -> {
            eventPublisher.publishEvent(cloudEvent);
        });
    }
}
