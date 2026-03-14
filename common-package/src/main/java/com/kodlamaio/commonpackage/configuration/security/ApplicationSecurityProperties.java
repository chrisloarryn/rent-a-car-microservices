package com.kodlamaio.commonpackage.configuration.security;

import com.kodlamaio.commonpackage.utils.constants.InternalApiHeaders;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security")
public class ApplicationSecurityProperties
{
    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://localhost:3000",
            "http://localhost:9010"));
    private String internalApiKeyHeader = InternalApiHeaders.ApiKey;
    private String internalApiKey = "rent-a-car-internal-dev-key";
    private BasicUsers basic = new BasicUsers();

    @Getter
    @Setter
    public static class BasicUsers
    {
        private BasicUser user = new BasicUser("app-user", "app-user-pass", List.of("user"));
        private BasicUser admin = new BasicUser("app-admin", "app-admin-pass", List.of("admin", "user"));
        private BasicUser moderator = new BasicUser("app-moderator", "app-moderator-pass", List.of("moderator", "user"));
    }

    @Getter
    @Setter
    public static class BasicUser
    {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();

        public BasicUser()
        {
        }

        public BasicUser(String username, String password, List<String> roles)
        {
            this.username = username;
            this.password = password;
            this.roles = new ArrayList<>(roles);
        }
    }
}
