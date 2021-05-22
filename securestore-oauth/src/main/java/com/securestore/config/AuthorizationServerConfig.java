package com.securestore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableAuthorizationServer
/**
 * 
 * @EnableAuthorizationServer annotation imports two configuration classes —
 * AuthorizationServerEndpointsConfiguration and AuthorizationServerSecurityConfiguration.
 * The AuthorizationServerEndpointsConfiguration configuration class create beans for three REST controllers —
 * AuthorizationEndpoint, TokenEndpoint, and CheckTokenEndpoint.
 * These three controllers provide implementations for endpoints specified in OAuth2 specification.
 * The AuthorizationServerSecurityConfiguration configuration class configures Spring Security for OAuth endpoints.
 * @author johne
 *
 */
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final String CLIENT_ID = "my-client";
    static final String CLIENT_SECRET = "{noop}my-secret";

    private static String REALM="MY_OAUTH_REALM";

    @Autowired private ClientDetailsService clientDetailsService;

    @Autowired private TokenStore tokenStore;

    @Autowired private UserApprovalHandler userApprovalHandler;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;


    /**
     * I had to define another passwordEncoder even for no encryption because there is already a passwordEncoder bean in the 
     * runtime and it's BCryptPasswordEncoder and hence this code also uses that bean causing the authentication to fail
     */
	@Autowired
	private PasswordEncoder passwordEncoder1;


    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.inMemory()
                .withClient(CLIENT_ID)
                .authorizedGrantTypes("authorization_code", "password", "refresh_token")
                //.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                .scopes("user_info", "read", "write", "trust")
                .secret(passwordEncoder1.encode(CLIENT_SECRET))
                .accessTokenValiditySeconds(120)
                .refreshTokenValiditySeconds(600)
                .autoApprove(true);
                //.redirectUris("http://localhost:8082/app1/login/oauth2/code/my-auth-server", "http://localhost:8083/app2/login/oauth2/code/my-auth-server");
        //redirect uris matter only for authorization_code and not for password grant type
        //Refresh token is only valid for 10 minutes.
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore)
                .userApprovalHandler(userApprovalHandler)
                .authenticationManager(authenticationManager);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        //oauthServer.realm(REALM+"/client");
        oauthServer.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();;
    }



    @Bean
    @SuppressWarnings("deprecation")
    public static NoOpPasswordEncoder passwordEncoder1() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    @Autowired
    public TokenStoreUserApprovalHandler userApprovalHandler(TokenStore tokenStore){
        TokenStoreUserApprovalHandler handler = new TokenStoreUserApprovalHandler();
        handler.setTokenStore(tokenStore);
        handler.setRequestFactory(new DefaultOAuth2RequestFactory(clientDetailsService));
        handler.setClientDetailsService(clientDetailsService);
        return handler;
    }

    @Bean
    @Autowired
    public ApprovalStore approvalStore(TokenStore tokenStore) throws Exception {
        TokenApprovalStore store = new TokenApprovalStore();
        store.setTokenStore(tokenStore);
        return store;
    }
}