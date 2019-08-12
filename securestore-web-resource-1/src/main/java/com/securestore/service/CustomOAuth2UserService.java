package com.securestore.service;

import com.securestore.domain.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    @Autowired
    private AuthoritiesExtractor authoritiesExtractor;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("OAuth2UserRequest.toString() : " + userRequest.toString());
        System.out.println("OAuth2UserRequest.getAccessToken() : " + userRequest.getAccessToken());
        System.out.println("OAuth2UserRequest.getAdditionalParameters() : " + userRequest.getAdditionalParameters());
        System.out.println("OAuth2UserRequest.getClientRegistration() : " + userRequest.getClientRegistration());

        System.out.println("OAuth2User.toString() : " + oAuth2User.toString());
        System.out.println("OAuth2User.getName() : " + oAuth2User.getName());
        System.out.println("OAuth2User.getAttributes() : " + oAuth2User.getAttributes());
        System.out.println("OAuth2User.getAuthorities() : " + oAuth2User.getAuthorities());

        List<GrantedAuthority> grantedAuthorityList = authoritiesExtractor.extractAuthorities(oAuth2User.getAttributes());

        return new CustomOAuth2User(grantedAuthorityList, oAuth2User.getAttributes());
    }
}