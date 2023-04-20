package org.eaip.rsocket.events;

import org.eaip.rsocket.cloudevents.CloudEventImpl;
import reactor.core.publisher.Mono;

/**
 * CloudEvents Consumer
 *
 * @author CuiCHangHe
 */
public interface CloudEventsConsumer {

    boolean shouldAccept(CloudEventImpl<?> cloudEvent);

    Mono<Void> accept(CloudEventImpl<?> cloudEvent);
}
