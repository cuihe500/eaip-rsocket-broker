package org.eaip.rsocket.gateway.auth;

import org.jetbrains.annotations.Nullable;

/**
 * JWT auth service
 *
 * @author CuiChangHe
 */
public interface JwtAuthenticationService {
    @Nullable
    NamedPrincipal auth(String jwtToken);
}
