package com.securestore.config;

import com.securestore.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * AbstractUserDetailsAuthenticationProvider is a base AuthenticationProvider that allows subclasses to override and work with UserDetails objects.
 * The class is designed to respond to UsernamePasswordAuthenticationToken authentication requests.
 * Upon successful validation, a UsernamePasswordAuthenticationToken will be created and returned to the caller.
 * The token will include as its principal either a String representation of the username, or the UserDetails that was returned from the authentication repository.
 *
 * Caching is handled by storing the UserDetails object being placed in the UserCache. This ensures that subsequent requests with the same username can be validated without needing to query the UserDetailsService. It should be noted that if a user appears to present an incorrect password, the UserDetailsService will be queried to confirm the most up-to-date password was used for comparison.
 * Caching is only likely to be required for stateless applications. In a normal web application, for example, the SecurityContext is stored in the user's session and the user isn't reauthenticated on each request. The default cache implementation is therefore NullUserCache.
 */
public class UserAccountAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    /**
     * The plaintext password used to perform
     * PasswordEncoder#matches(CharSequence, String)}  on when the user is
     * not found to avoid SEC-2056.
     */
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserAccountDetailsService userDetailsService;

    /**
     * The password used to perform
     * {@link PasswordEncoder#matches(CharSequence, String)} on when the user is
     * not found to avoid SEC-2056. This is necessary, because some
     * {@link PasswordEncoder} implementations will short circuit if the password is not
     * in a valid format.
     */
    private String userNotFoundEncodedPassword;

    public UserAccountAuthenticationProvider(PasswordEncoder passwordEncoder, UserAccountDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) 
        throws AuthenticationException {
        logger.debug("UserAccountAuthenticationProvider::additionalAuthenticationChecks");
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        String incomingRawPassword = authentication.getCredentials()
            .toString();

        if (!passwordEncoder.matches(incomingRawPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");
            throw new BadCredentialsException(
                messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    @Override
    protected void doAfterPropertiesSet()  {
        Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
        this.userNotFoundEncodedPassword = this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) 
        throws AuthenticationException {
        logger.debug("UserAccountAuthenticationProvider::retrieveUser");
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        UserDetails loadedUser;
        try {
            logger.debug("loading user using "+auth.getPrincipal().toString()+":"+auth.getDomain());
            loadedUser = this.userDetailsService.loadUserByUsernameAndDomain(auth.getName(), auth.getDomain());
            logger.debug("loadedUser: " + loadedUser);
        } catch (UsernameNotFoundException notFound) {
            if (authentication.getCredentials() != null) {
                String presentedPassword = authentication.getCredentials()
                    .toString();
                passwordEncoder.matches(presentedPassword, userNotFoundEncodedPassword);
            }
            throw notFound;
        } catch (Exception repositoryProblem) {
            throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException("UserDetailsService returned null, "
                + "which is an interface contract violation");
        }
        return loadedUser;
    }
}