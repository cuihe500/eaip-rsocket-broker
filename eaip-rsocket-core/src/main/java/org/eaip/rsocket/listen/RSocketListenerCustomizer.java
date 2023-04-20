package org.eaip.rsocket.listen;

/**
 * RSocket listener customizer
 *
 * @author CuiCHangHe
 */
@FunctionalInterface
public interface RSocketListenerCustomizer {

    void customize(RSocketListener.Builder builder);

}
