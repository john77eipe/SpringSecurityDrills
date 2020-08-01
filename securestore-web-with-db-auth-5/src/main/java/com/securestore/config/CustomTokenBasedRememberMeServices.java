package com.securestore.config;

import com.securestore.domain.CustomSecurityUser;
import com.securestore.domain.UserAccount;
import com.securestore.service.UserAccountDetailsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

/**
 * Identifies previously remembered users by a Base-64 encoded cookie.
 *
 * <p>
 * This implementation does not rely on an external database, so is attractive for simple
 * applications. The cookie will be valid for a specific period from the date of the last
 * {@link #loginSuccess(HttpServletRequest, HttpServletResponse, Authentication)}. As per
 * the interface contract, this method will only be called when the principal completes a
 * successful interactive authentication. As such the time period commences from the last
 * authentication attempt where they furnished credentials - not the time period they last
 * logged in via remember-me. The implementation will only send a remember-me token if the
 * parameter defined by {@link #setParameter(String)} is present.
 * <p>
 * An {@link org.springframework.security.core.userdetails.UserDetailsService} is required
 * by this implementation, so that it can construct a valid <code>Authentication</code>
 * from the returned {@link org.springframework.security.core.userdetails.UserDetails}.
 * This is also necessary so that the user's password is available and can be checked as
 * part of the encoded cookie.
 * <p>
 * The cookie encoded by this implementation adopts the following form:
 *
 * <pre>
 * username + &quot;:&quot; + domain + &quot;:&quot; + expiryTime + &quot;:&quot;
 * 		+ Md5Hex(username + &quot;:&quot; + domain + &quot;:&quot; + expiryTime + &quot;:&quot; + password + &quot;:&quot; + key)
 * </pre>
 *
 * <p>
 * As such, if the user changes their password, any remember-me token will be invalidated.
 * Equally, the system administrator may invalidate every remember-me token on issue by
 * changing the key. This provides some reasonable approaches to recovering from a
 * remember-me token being left on a public machine (e.g. kiosk system, Internet cafe
 * etc). Most importantly, at no time is the user's password ever sent to the user agent,
 * providing an important security safeguard. Unfortunately the username is necessary in
 * this implementation (as we do not want to rely on a database for remember-me services).
 * High security applications should be aware of this occasionally undesired disclosure of
 * a valid username.
 * <p>
 * This is a basic remember-me implementation which is suitable for many applications.
 * However, we recommend a database-based implementation if you require a more secure
 * remember-me approach (see {PersistentTokenBasedRememberMeServices}).
 * <p>
 * By default the tokens will be valid for 14 days from the last successful authentication
 * attempt. This can be changed using {@link #setTokenValiditySeconds(int)}. If this value
 * is less than zero, the <tt>expiryTime</tt> will remain at 14 days, but the negative
 * value will be used for the <tt>maxAge</tt> property of the cookie, meaning that it will
 * not be stored when the browser is closed.
 *
 *
 * @author John Eipe
 */
public class CustomTokenBasedRememberMeServices extends AbstractRememberMeServices {

	protected final Log logger = LogFactory.getLog(getClass());

	public CustomTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService) {
		super(key, userDetailsService);
	}

	@Override
	protected UserDetails processAutoLoginCookie(String[] cookieTokens,
			HttpServletRequest request, HttpServletResponse response) {

		logger.debug("CustomTokenBasedRememberMeServices::processAutoLoginCookie");

		if (cookieTokens.length != 4) {
			throw new InvalidCookieException("Cookie token did not contain 4"
					+ " tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
		}

		long tokenExpiryTime;

		try {
			tokenExpiryTime = new Long(cookieTokens[2]);
		}
		catch (NumberFormatException nfe) {
			throw new InvalidCookieException(
					"Cookie token[1] did not contain a valid number (contained '"
							+ cookieTokens[2] + "')");
		}

		if (isTokenExpired(tokenExpiryTime)) {
			throw new InvalidCookieException("Cookie token[1] has expired (expired on '"
					+ new Date(tokenExpiryTime) + "'; current time is '" + new Date()
					+ "')");
		}

		// Check the user exists.
		// Defer lookup until after expiry time checked, to possibly avoid expensive
		// database call.
		UserAccountDetailsService userAccountDetailsService = (UserAccountDetailsService) getUserDetailsService();
		CustomSecurityUser userDetails = (CustomSecurityUser) userAccountDetailsService.loadUserByUsernameAndDomain(cookieTokens[0], cookieTokens[1]);


		Assert.notNull(userDetails, () -> "UserDetailsService " + getUserDetailsService()
				+ " returned null for username " + cookieTokens[0] + "and domain " + cookieTokens[1] +". "
				+ "This is an interface contract violation");

		// Check signature of token matches remaining details.
		// Must do this after user lookup, as we need the DAO-derived password.
		// If efficiency was a major issue, just add in a UserCache implementation,
		// but recall that this method is usually only called once per HttpSession - if
		// the token is valid,
		// it will cause SecurityContextHolder population, whilst if invalid, will cause
		// the cookie to be cancelled.
		String expectedTokenSignature = makeTokenSignature(tokenExpiryTime,
				userDetails.getUsername(), userDetails.getDomain(), userDetails.getPassword());

		if (!equals(expectedTokenSignature, cookieTokens[3])) {
			throw new InvalidCookieException("Cookie token[3] contained signature '"
					+ cookieTokens[3] + "' but expected '" + expectedTokenSignature + "'");
		}

		return userDetails;
	}

	/**
	 * Calculates the digital signature to be put in the cookie. Default value is MD5
	 * ("username:tokenExpiryTime:password:key")
	 */
	protected String makeTokenSignature(long tokenExpiryTime, String username, String domain,
			String password) {
		String data = username + ":" + domain + ":" + tokenExpiryTime + ":" + password + ":" + getKey();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		return new String(Hex.encode(digest.digest(data.getBytes())));
	}

	protected boolean isTokenExpired(long tokenExpiryTime) {
		return tokenExpiryTime < System.currentTimeMillis();
	}

	@Override
	public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {

		logger.debug("CustomTokenBasedRememberMeServices::onLoginSuccess");

		String username = ((CustomSecurityUser) authentication.getPrincipal()).getUsername();
		String domain = ((CustomSecurityUser) authentication.getPrincipal()).getDomain();
		String password = ((CustomSecurityUser) authentication.getPrincipal()).getPassword();

		UserAccountDetailsService userAccountDetailsService = (UserAccountDetailsService) getUserDetailsService();

		// If unable to find a username and password, just abort as
		// TokenBasedRememberMeServices is
		// unable to construct a valid token in this case.
		if (!StringUtils.hasLength(username)) {
			logger.debug("Unable to retrieve username");
			return;
		}

		if (!StringUtils.hasLength(password)) {
			CustomSecurityUser user = (CustomSecurityUser) userAccountDetailsService.loadUserByUsernameAndDomain(username, domain);
			password = user.getPassword();

			if (!StringUtils.hasLength(password)) {
				logger.debug("Unable to obtain password for user: " + username);
				return;
			}
		}

		int tokenLifetime = calculateLoginLifetime(request, authentication);
		long expiryTime = System.currentTimeMillis();
		// SEC-949
		expiryTime += 1000L * (tokenLifetime < 0 ? TWO_WEEKS_S : tokenLifetime);

		String signatureValue = makeTokenSignature(expiryTime, username, domain, password);

		setCookie(new String[] { username, domain, Long.toString(expiryTime), signatureValue },
				tokenLifetime, request, response);

		if (logger.isDebugEnabled()) {
			logger.debug("Added remember-me cookie for user '" + username
					+ "', expiry: '" + new Date(expiryTime) + "'");
		}
	}

	/**
	 * Calculates the validity period in seconds for a newly generated remember-me login.
	 * After this period (from the current time) the remember-me login will be considered
	 * expired. This method allows customization based on request parameters supplied with
	 * the login or information in the <tt>Authentication</tt> object. The default value
	 * is just the token validity period property, <tt>tokenValiditySeconds</tt>.
	 * <p>
	 * The returned value will be used to work out the expiry time of the token and will
	 * also be used to set the <tt>maxAge</tt> property of the cookie.
	 *
	 * See SEC-485.
	 *
	 * @param request the request passed to onLoginSuccess
	 * @param authentication the successful authentication object.
	 * @return the lifetime in seconds.
	 */
	protected int calculateLoginLifetime(HttpServletRequest request,
			Authentication authentication) {
		return getTokenValiditySeconds();
	}


	/**
	 * Constant time comparison to prevent against timing attacks.
	 */
	private static boolean equals(String expected, String actual) {
		byte[] expectedBytes = bytesUtf8(expected);
		byte[] actualBytes = bytesUtf8(actual);

		return MessageDigest.isEqual(expectedBytes, actualBytes);
	}

	private static byte[] bytesUtf8(String s) {
		if (s == null) {
			return null;
		}
		return Utf8.encode(s);
	}
}