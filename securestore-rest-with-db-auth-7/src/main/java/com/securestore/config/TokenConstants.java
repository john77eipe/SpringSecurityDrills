package com.securestore.config;

public class TokenConstants {
    public static final long TOKEN_LIFETIME = 604_800_000; // That's 7 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_SECRET = "ThisIsOurSecretKeyToSignOurTokens";
}
