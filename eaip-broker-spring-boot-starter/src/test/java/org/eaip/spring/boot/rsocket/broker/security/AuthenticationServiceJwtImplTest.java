package org.eaip.spring.boot.rsocket.broker.security;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.UUID;

/**
 * AuthenticationServiceJwtImpl test
 *
 * @author CuiChangHe
 */
public class AuthenticationServiceJwtImplTest {
    public AuthenticationServiceJwtImpl authenticationService;

    @BeforeAll
    public void setUp() throws Exception {
        this.authenticationService = new AuthenticationServiceJwtImpl();
    }

    @Test
    public void testAuth() throws Exception {
        String subject = "testing-only";
        String credentials = authenticationService.generateCredentials(UUID.randomUUID().toString(), new String[]{"eaip"}, new String[]{"default"}, new String[]{"internal"}, null, subject, new String[]{"cuichanghe"});
        System.out.println(credentials);
        Principal principal = authenticationService.auth("JWT", credentials);
        Assertions.assertNotNull(principal);
        Assertions.assertEquals(subject, principal.getName());
    }

}
