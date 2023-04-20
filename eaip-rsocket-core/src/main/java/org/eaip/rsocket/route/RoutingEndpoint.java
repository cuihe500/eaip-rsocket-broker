package org.eaip.rsocket.route;

import org.eaip.rsocket.metadata.GSVRoutingMetadata;

import java.util.List;

/**
 * routing endpoint
 *
 * @author CuiCHangHe
 */
public class RoutingEndpoint extends GSVRoutingMetadata {
    /**
     * uri list
     */
    private List<String> uris;

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }
}
