package org.eaip.spring.boot.rsocket.upstream;

/**
 * rsocket requester support customizer
 *
 * @author CuiChangHe
 */
@FunctionalInterface
public interface RSocketRequesterSupportCustomizer {

    void customize(RSocketRequesterSupportBuilder builder);
}
