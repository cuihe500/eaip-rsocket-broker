package org.eaip.rsocket.cloudevents;

import reactor.core.publisher.Mono;

/**
 * CloudEvents Notify service
 *
 * @author CuiCHangHe
 */
public interface CloudEventsNotifyService {

    public Mono<Void> notify(String appId, String cloudEventJson);

    public Mono<Void> notifyAll(String appName, String cloudEventJson);
}
