package com.kodlamaio.apigateway;

import com.kodlamaio.commonpackage.configuration.security.ApplicationSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactiveSecurityConfigTests
{
    @Test
    void securityConfigBuildsRuntimeBeans()
    {
        ReactiveSecurityConfig config = new ReactiveSecurityConfig();
        ApplicationSecurityProperties properties = new ApplicationSecurityProperties();
        MapReactiveUserDetailsService userDetailsService = config.reactiveUserDetailsService(
                properties,
                config.passwordEncoder());
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.authenticationManager(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService));

        SecurityWebFilterChain filterChain = config.springSecurityFilterChain(
                http,
                new StaticListableBeanFactory().getBeanProvider(org.springframework.security.oauth2.jwt.ReactiveJwtDecoder.class));

        assertNotNull(userDetailsService);
        assertNotNull(filterChain);

        StepVerifier.create(filterChain.getWebFilters().count())
                .expectNextMatches(count -> count > 0)
                .verifyComplete();
    }

    @Test
    void securityConfigExposesConfiguredCorsAndReactiveUsers()
    {
        ReactiveSecurityConfig config = new ReactiveSecurityConfig();
        ApplicationSecurityProperties properties = new ApplicationSecurityProperties();
        properties.setAllowedOrigins(List.of("https://frontend.example", "https://admin.example"));
        MapReactiveUserDetailsService userDetailsService = config.reactiveUserDetailsService(
                properties,
                config.passwordEncoder());
        CorsConfigurationSource corsConfigurationSource = config.corsConfigurationSource(properties);

        StepVerifier.create(userDetailsService.findByUsername("app-user"))
                .assertNext(user -> {
                    assertEquals("app-user", user.getUsername());
                    assertTrue(user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_USER")));
                })
                .verifyComplete();
        StepVerifier.create(userDetailsService.findByUsername("app-admin"))
                .assertNext(user -> assertTrue(user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_ADMIN"))))
                .verifyComplete();
        StepVerifier.create(userDetailsService.findByUsername("app-moderator"))
                .assertNext(user -> assertTrue(user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_MODERATOR"))))
                .verifyComplete();

        var corsConfiguration = corsConfigurationSource.getCorsConfiguration(
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/system/ping").build()));

        assertNotNull(corsConfiguration);
        assertEquals(List.of("https://frontend.example", "https://admin.example"), corsConfiguration.getAllowedOrigins());
        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
        assertTrue(corsConfiguration.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void securityConfigBuildsOauth2BranchWhenReactiveJwtDecoderExists()
    {
        ReactiveSecurityConfig config = new ReactiveSecurityConfig();
        ApplicationSecurityProperties properties = new ApplicationSecurityProperties();
        MapReactiveUserDetailsService userDetailsService = config.reactiveUserDetailsService(
                properties,
                config.passwordEncoder());
        ServerHttpSecurity http = ServerHttpSecurity.http();
        http.authenticationManager(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService));
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("reactiveJwtDecoder", (ReactiveJwtDecoder) token -> Mono.error(new UnsupportedOperationException("not used in this test")));

        SecurityWebFilterChain filterChain = config.springSecurityFilterChain(
                http,
                beanFactory.getBeanProvider(ReactiveJwtDecoder.class));

        assertNotNull(filterChain);
        StepVerifier.create(filterChain.getWebFilters().count())
                .expectNextMatches(count -> count > 0)
                .verifyComplete();
    }
}
