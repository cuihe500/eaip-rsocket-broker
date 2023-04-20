package org.eaip.spring.boot.rsocket.broker.route;

import org.eaip.spring.boot.rsocket.broker.security.RSocketAppPrincipal;

/**
 * Service Mesh inspector
 *
 * @author CuiChangHe
 */
public interface ServiceMeshInspector {

    boolean isRequestAllowed(RSocketAppPrincipal requesterPrincipal, String routing, RSocketAppPrincipal responderPrincipal);

    Integer getWhiteRelationCount();
}
