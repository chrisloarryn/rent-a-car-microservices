package com.kodlamaio.commonpackage.configuration.security;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test & !gatling")
public class InternalApiKeyFeignConfiguration
{
    @Bean
    RequestInterceptor internalApiKeyRequestInterceptor(ApplicationSecurityProperties securityProperties)
    {
        return template -> template.header(
                securityProperties.getInternalApiKeyHeader(),
                securityProperties.getInternalApiKey());
    }
}
