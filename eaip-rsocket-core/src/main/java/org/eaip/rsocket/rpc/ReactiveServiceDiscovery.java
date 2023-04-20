package org.eaip.rsocket.rpc;

import org.eaip.rsocket.rpc.definition.ReactiveServiceInterface;

/**
 * Reactive Service Discovery
 *
 * @author CuiCHangHe
 */
public interface ReactiveServiceDiscovery {

    ReactiveServiceInterface findServiceByFullName(String serviceFullName);
}
