package org.eaip.spring.boot.rsocket.upstream;

import io.rsocket.plugins.RSocketInterceptor;
import org.eaip.rsocket.RSocketRequesterSupport;

/**
 * RSocket Requester support builder
 *
 * @author CuiChangHe
 */
public interface RSocketRequesterSupportBuilder {

    RSocketRequesterSupportBuilder addResponderInterceptor(RSocketInterceptor interceptor);

    RSocketRequesterSupportBuilder addRequesterInterceptor(RSocketInterceptor interceptor);

    RSocketRequesterSupport build();
}
