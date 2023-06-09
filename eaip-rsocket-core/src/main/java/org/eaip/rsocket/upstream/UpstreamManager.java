package org.eaip.rsocket.upstream;

import io.rsocket.RSocket;
import org.eaip.rsocket.Initializable;
import org.eaip.rsocket.RSocketRequesterSupport;
import org.eaip.rsocket.discovery.DiscoveryService;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;

/**
 * upstream manager
 *
 * @author CuiCHangHe
 */
public interface UpstreamManager extends Initializable, Closeable {

    void add(UpstreamCluster cluster);

    void remove(UpstreamCluster cluster);

    void addP2pService(String serviceId);

    Collection<UpstreamCluster> findAllClusters();

    UpstreamCluster findClusterByServiceId(String serviceId);

    UpstreamCluster findBroker();

    DiscoveryService findBrokerDiscoveryService();

    /**
     * get rsocket for service id with load balance support
     *
     * @param serviceId service id
     * @return rsocket
     */
    RSocket getRSocket(String serviceId);

    RSocketRequesterSupport requesterSupport();

    /**
     * refresh service  with new uri list
     *
     * @param serviceId service id
     * @param uris      uri list
     */
    void refresh(String serviceId, List<String> uris);

    void init() throws Exception;

    void close();
}
