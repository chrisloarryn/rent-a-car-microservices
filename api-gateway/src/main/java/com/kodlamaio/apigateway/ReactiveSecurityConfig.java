package com.kodlamaio.apigateway;

import com.kodlamaio.commonpackage.configuration.security.ApplicationSecurityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Profile("!test & !gatling")
@EnableWebFluxSecurity
@EnableConfigurationProperties(ApplicationSecurityProperties.class)
public class ReactiveSecurityConfig
{
    @Bean
    PasswordEncoder passwordEncoder()
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    MapReactiveUserDetailsService reactiveUserDetailsService(
            ApplicationSecurityProperties securityProperties,
            PasswordEncoder passwordEncoder)
    {
        ApplicationSecurityProperties.BasicUsers users = securityProperties.getBasic();

        return new MapReactiveUserDetailsService(
                User.withUsername(users.getUser().getUsername())
                        .password(passwordEncoder.encode(users.getUser().getPassword()))
                        .roles(users.getUser().getRoles().toArray(String[]::new))
                        .build(),
                User.withUsername(users.getAdmin().getUsername())
                        .password(passwordEncoder.encode(users.getAdmin().getPassword()))
                        .roles(users.getAdmin().getRoles().toArray(String[]::new))
                        .build(),
                User.withUsername(users.getModerator().getUsername())
                        .password(passwordEncoder.encode(users.getModerator().getPassword()))
                        .roles(users.getModerator().getRoles().toArray(String[]::new))
                        .build());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(ApplicationSecurityProperties securityProperties)
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ObjectProvider<ReactiveJwtDecoder> jwtDecoderProvider)
    {
        ReactiveJwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/api/system/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus")
                        .permitAll()
                        .anyExchange()
                        .authenticated());

        if (jwtDecoder != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)));
        }

        return http.build();
    }
}
