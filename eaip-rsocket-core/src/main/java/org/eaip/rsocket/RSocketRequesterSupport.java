package org.eaip.rsocket;

import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.plugins.RSocketInterceptor;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.events.ServicesExposedEvent;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * RSocket requester support: setup payload, exposed services, acceptor, plugins
 *
 * @author CuiChangHe
 */
public interface RSocketRequesterSupport {

    URI originUri();

    Supplier<Payload> setupPayload(String serviceId);

    Supplier<Set<ServiceLocator>> exposedServices();

    Supplier<Set<ServiceLocator>> subscribedServices();

    Supplier<CloudEventImpl<ServicesExposedEvent>> servicesExposedEvent();

    SocketAcceptor socketAcceptor();

    List<RSocketInterceptor> responderInterceptors();

    List<RSocketInterceptor> requestInterceptors();
}
