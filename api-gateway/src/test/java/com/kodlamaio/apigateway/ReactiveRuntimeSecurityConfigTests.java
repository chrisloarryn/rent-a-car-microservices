package com.kodlamaio.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReactiveRuntimeSecurityConfigTests
{
    @Test
    void securityConfigCanBeInstantiated()
    {
        assertNotNull(new ReactiveRuntimeSecurityConfig());
    }

    @Test
    void securityFilterChainCanBeBuiltForTestAndGatlingProfiles()
    {
        ReactiveRuntimeSecurityConfig config = new ReactiveRuntimeSecurityConfig();
        SecurityWebFilterChain filterChain = config.springSecurityFilterChain(ServerHttpSecurity.http());

        assertNotNull(filterChain);
        StepVerifier.create(filterChain.getWebFilters().count())
                .expectNextMatches(count -> count > 0)
                .verifyComplete();
    }
}
