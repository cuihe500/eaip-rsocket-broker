package org.eaip.spring.boot.rsocket.broker.responder;

import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;
import org.eaip.spring.boot.rsocket.broker.BrokerAppContext;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * broadcast spread for cloudevents
 *
 * @author CuiChangHe
 */
@SuppressWarnings("rawtypes")
public interface BroadcastSpread {

    Mono<Void> send(@NotNull String appUUID, final CloudEventImpl cloudEvent);

    Mono<Void> broadcast(@NotNull String appName, final CloudEventImpl cloudEvent);

    Mono<Void> broadcastAll(CloudEventImpl cloudEvent);

    default CloudEventImpl<Map<String, Object>> buildMapCloudEvent(@NotNull String type, @NotNull String subject, @NotNull Map<String, Object> data) {
        return RSocketCloudEventBuilder.builder(data)
                .withSource(BrokerAppContext.identity())
                .withType(type)
                .withSubject(subject)
                .build();
    }
}
