package org.eaip.rsocket.gateway.auth;

import java.security.Principal;

/**
 * named Principal
 *
 * @author CuiChangHe
 */
public class NamedPrincipal implements Principal {
    private String name;

    public NamedPrincipal() {

    }

    public NamedPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
