package com.kodlamaio.commonpackage.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodlamaio.commonpackage.utils.security.KeycloakRoleConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Profile("!test & !gatling")
@EnableMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(ApplicationSecurityProperties.class)
public class SecurityConfig
{
    @Bean
    public CorsConfigurationSource corsConfigurationSource(ApplicationSecurityProperties securityProperties)
    {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder()
    {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            ApplicationSecurityProperties securityProperties,
            PasswordEncoder passwordEncoder)
    {
        ApplicationSecurityProperties.BasicUsers users = securityProperties.getBasic();
        return new InMemoryUserDetailsManager(
                buildUser(users.getUser(), passwordEncoder),
                buildUser(users.getAdmin(), passwordEncoder),
                buildUser(users.getModerator(), passwordEncoder));
    }

    @Bean
    InternalApiKeyFilter internalApiKeyFilter(
            ApplicationSecurityProperties securityProperties,
            ObjectMapper objectMapper)
    {
        return new InternalApiKeyFilter(securityProperties, objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            InternalApiKeyFilter internalApiKeyFilter,
            ObjectProvider<JwtDecoder> jwtDecoderProvider) throws Exception
    {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(internalApiKeyFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/system/ping")
                        .permitAll()
                        .requestMatchers("/api/internal/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .hasAnyRole("user", "admin", "moderator")
                        .anyRequest()
                        .authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        if (jwtDecoderProvider.getIfAvailable() != null) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        }

        return http.build();
    }

    private UserDetails buildUser(
            ApplicationSecurityProperties.BasicUser properties,
            PasswordEncoder passwordEncoder)
    {
        return User.withUsername(properties.getUsername())
                .password(passwordEncoder.encode(properties.getPassword()))
                .roles(properties.getRoles().toArray(String[]::new))
                .build();
    }
}
