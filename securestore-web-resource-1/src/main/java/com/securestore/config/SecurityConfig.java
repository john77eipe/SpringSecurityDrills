package com.securestore.config;

import com.securestore.domain.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;

@EnableOAuth2Client
@Configuration

public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.client.provider.my-auth-server.logoutSuccessUrl}")
    private String logoutSuccessUrl;

    @Autowired
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public OAuth2RestOperations restTemplate(OAuth2ClientContext oauth2ClientContext2) {
        return new OAuth2RestTemplate(resource(), oauth2ClientContext2);
    }

    @Bean
    protected OAuth2ProtectedResourceDetails resource() {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("custom");
        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        List scopes = new ArrayList<String>(2);
        scopes.add("write");
        scopes.add("read");
        resource.setAccessTokenUri(clientRegistration.getProviderDetails().getTokenUri());
        resource.setClientId(clientRegistration.getClientId());
        resource.setClientSecret(clientRegistration.getClientSecret());
        resource.setScope(scopes);
        return resource;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .customUserType(CustomOAuth2User.class, "custom")
                .userService(oAuth2UserService);
    }
}
