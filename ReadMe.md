# Spring Security

At its core, Spring Security is really just a bunch of servlet filters that help you add authentication and authorization to your web application.

It also integrates well with frameworks like Spring Web MVC (or Spring Boot) as well as with standards like OAuth2 or SAML. And it auto-generates login/logout pages and protects against common exploits like CSRF.

Before that remember - Spring Web/Rest is nothing but a DispatcherServlet, Servlet Filters and some utility methods that works in tandum with Spring Core (the dependency injection engine)

Spring Security essentially provides a bunch of features mostly by wiring more Servlet Filters.

Why Filters?

There is no security hardcoded into that DispatcherServlet and you also very likely don’t want to fumble around with a raw HTTP Basic Auth header in your @Controllers. Optimally, the authentication and authorization should be done *before* a request hits your @Controllers.

The below explanation is from https://www.marcobehler.com

#### A naive SecurityFilter

A SecurityFilter has roughly 4 tasks and a naive and overly-simplified implementation could look like this:

```java
import javax.servlet.*;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityServletFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        UsernamePasswordToken token = extractUsernameAndPasswordFrom(request);  // (1)

        if (notAuthenticated(token)) {  // (2)
            // either no or wrong username/password
            // unfortunately the HTTP status code is called "unauthorized", instead of "unauthenticated"
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401.
            return;
        }

        if (notAuthorized(token, request)) { // (3)
            // you are logged in, but don't have the proper rights
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
            return;
        }

        // allow the HttpRequest to go to Spring's DispatcherServlet
        // and @RestControllers/@Controllers.
        chain.doFilter(request, response); // (4)
    }

    private UsernamePasswordToken extractUsernameAndPasswordFrom(HttpServletRequest request) {
        // Either try and read in a Basic Auth HTTP Header, which comes in the form of user:password
        // Or try and find form login request parameters or POST bodies, i.e. "username=me" & "password="myPass"
        return checkVariousLoginOptions(request);
    }


    private boolean notAuthenticated(UsernamePasswordToken token) {
        // compare the token with what you have in your database...or in-memory...or in LDAP...
        return false;
    }

    private boolean notAuthorized(UsernamePasswordToken token, HttpServletRequest request) {
       // check if currently authenticated user has the permission/role to access this request's /URI
       // e.g. /admin needs a ROLE_ADMIN , /callcenter needs ROLE_CALLCENTER, etc.
       return false;
    }
}
```

1. First, the filter needs to extract a username/password from the request. It could be via a [Basic Auth HTTP Header](https://en.wikipedia.org/wiki/Basic_access_authentication), or form fields, or a cookie, etc.
2. Then the filter needs to validate that username/password combination against *something*, like a database.
3. The filter needs to check, after successful authentication, that the user is authorized to access the requested URI.
4. If the request *survives* all these checks, then the filter can let the request go through to your DispatcherServlet, i.e. your @Controllers.

#### FilterChains

Reality Check: While the above code works compiles, it would sooner or later lead to one monster filter with a ton of code for various authentication and authorization mechanisms.

In the real-world, however, you would split this one filter up into *multiple* filters, that you then *chain* together.

For example, an incoming HTTP request would…

1. First, go through a LoginMethodFilter…
2. Then, go through an AuthenticationFilter…
3. Then, go through an AuthorizationFilter…
4. Finally, hit your servlet.

This concept is called *FilterChain* and the last method call in your filter above is actually delegating to that very chain:

```java
chain.doFilter(request, response);
```

With such a filter (chain) you can basically handle every authentication or authorization problem there is in your application, without needing to change your actual application implementation (think: your @RestControllers / @Controllers).

### Spring’s DefaultSecurityFilterChain

When you boot up your web application. You’ll see the following log message:

```
2020-02-25 10:24:27.875  INFO 11116 --- [           main] o.s.s.web.DefaultSecurityFilterChain     : Creating filter chain: any request, [org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@46320c9a, org.springframework.security.web.context.SecurityContextPersistenceFilter@4d98e41b, org.springframework.security.web.header.HeaderWriterFilter@52bd9a27, org.springframework.security.web.csrf.CsrfFilter@51c65a43, org.springframework.security.web.authentication.logout.LogoutFilter@124d26ba, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter@61e86192, org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter@10980560, org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter@32256e68, org.springframework.security.web.authentication.www.BasicAuthenticationFilter@52d0f583, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@5696c927, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@5f025000, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@5e7abaf7, org.springframework.security.web.session.SessionManagementFilter@681c0ae6, org.springframework.security.web.access.ExceptionTranslationFilter@15639d09, org.springframework.security.web.access.intercept.FilterSecurityInterceptor@4f7be6c8]|
```

If you expand that one line into a list, it looks like Spring Security does not just install *one* filter, instead it installs a whole filter chain consisting of 15 (!) different filters.

So, when an HTTPRequest comes in, it will go through *all* these 15 filters, before your request finally hits your @RestControllers. The order is important, too, starting at the top of that list and going down to the bottom.

![filterchain 1a](https://www.marcobehler.com/images/filterchain-1a.png)

**Analyzing Spring’s FilterChain**

- **BasicAuthenticationFilter**: Tries to find a Basic Auth HTTP Header on the request and if found, tries to authenticate the user with the header’s username and password.
- **UsernamePasswordAuthenticationFilter**: Tries to find a username/password request parameter/POST body and if found, tries to authenticate the user with those values.
- **DefaultLoginPageGeneratingFilter**: Generates a login page for you, if you don’t explicitly disable that feature. THIS filter is why you get a default login page when enabling Spring Security.
- **DefaultLogoutPageGeneratingFilter**: Generates a logout page for you, if you don’t explicitly disable that feature.
- **FilterSecurityInterceptor**: Does your authorization.

So with these couple of filters, Spring Security provides you a login/logout page, as well as the ability to login with Basic Auth or Form Logins, as well as a couple of additional goodies like the CsrfFilter, that we are going to have a look at later.

All the experiments (Part 1-5) uses Form Logins and not Basic Auth. 

For basic auth have a look here: https://www.baeldung.com/spring-security-basic-authentication

### How to configure Spring Security: WebSecurityConfigurerAdapter

With the latest Spring Security and/or Spring Boot versions, the way to configure Spring Security is by having a class that:

1. Is annotated with @EnableWebSecurity.
2. Extends WebSecurityConfigurer, which basically offers you a configuration DSL/methods. With those methods, you can specify what URIs in your application to protect or what exploit protections to enable/disable.

**Note**: You would have noticed that even if adapter is not configured,  it comes with a pretty reasonable implementation - by default. This is what it looks like:

```java
public abstract class WebSecurityConfigurerAdapter implements
                WebSecurityConfigurer<WebSecurity> {

    protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                    .anyRequest().authenticated()  // (1)
                    .and()
                .formLogin().and()   // (2)
                .httpBasic();  // (3)
        }
}
```

1. To access *any* URI (`*anyRequest()*`) on your application, you need to authenticate (authenticated()).
2. Form Login (`*formLogin()*`) with default settings is enabled.
3. As is HTTP Basic authentication (`*httpBasic()*`).

*This* default configuration is why your application is on lock-down, as soon as you add Spring Security dependency to it. 

## Authentication

When it comes to authentication and Spring Security you have roughly three scenarios:

1. The **default**: You *can* access the (hashed) password of the user, because you have his details (username, password) saved in e.g. file/database/in-memory.
    In this approach we provide an implementation of `UserDetailsService` and override `loadUserByUsername` method to return an object of  type `UserDetails`
2. **Custom or ThirdParty**: You *cannot* access the (hashed) password of the user. This is the case if your users and passwords are stored *somewhere* else, like in a 3rd party identity management product offering REST services for authentication. Think: [Atlassian Crowd](https://www.atlassian.com/software/crowd).
    If that is the case, you cannot use a UserDetailsService anymore, instead you need to implement and provide an `AuthenticationProvider` by implementing `AuthenticationProvider` interface.  We then override the `authenticate` method to return an object of type `UsernamePasswordAuthenticationToken`.
3. **OAuth**: You want to use OAuth2 or "Login with Google/Twitter/etc." (OpenID), likely in combination with JWT. 




>  **The following experiments from (Part 1-4) take the default approach. Part 5-6 uses Custom approach and finally Part 7 and beyond OAuth approach.**





## Authorization

What are Authorities? What are Roles?

- An authority (in its simplest form) is just a string, it can be anything like: user, ADMIN, ROLE_ADMIN or 53cr37_r0l3.
- A role is an authority with a `*ROLE_*` prefix. So a role called `*ADMIN*` is the same as an authority called `*ROLE_ADMIN*`.

The distinction between roles and authorities is purely conceptual and something that often bewilders people new to Spring Security.

**Why is there a distinction between roles and authorities?**

Nothing

**What are GrantedAuthorities? What are SimpleGrantedAuthorities?**

Of course, Spring Security doesn’t let you get away with *just* using Strings. There’s a Java class representing your authority String, a popular one being SimpleGrantedAuthority.

```java
public final class SimpleGrantedAuthority implements GrantedAuthority {

        private final String role;

    @Override
        public String getAuthority() {
                return role;
        }
}
```

Note: There’s other authority classes as well.

1. For Approach (Authorization approach 1):
   Think, UserDetailsService.loadUserByUsername which returns a instance of `UserDetails`. For Authorities we create an instance of `GrantedAuthority`  and set it to UserDetails.

2. For Approach (Authorization approach 2): 
   When the users comes from a third-party application, like Atlassian Cloud, you’ll need to find out what concept they are using to support authorities. Atlassian Crowd had the concepts of "roles", but deprecated it in favour of "groups".

So, depending on the actual product you are using, you need to map this to a Spring Security authority, in your AuthenticationProvider.

**hasAccess and SpEL**
Last, but not least, the most powerful way to configure authorizations, is with the access method. It lets you specify pretty much any valid SpEL expressions.
```java
http
.authorizeRequests()
.antMatchers("/admin").access("hasRole('admin') and hasIpAddress('192.168.1.0/24') and @myCustomBean.checkAccess(authentication,request)") // (1)
```



## Part 1: Secure Web with In-Memory Authentication and Authorization with no encryption

Checkout the project: **securestore-web-with-db-auth-1**

There are only 3 kinds of users:

- anonymous
- authenticated user with ROLE: USER
- authenticated user with ROLE: ADMIN

There are only 3 pages:

- home page: accessible to all (also is the login page)
- profile page: accessible to all USERs
- manage-users: accessible to all ADMINs
- Login endpoint : managed by spring (note that we don't have a controller for it)
- Logout url: managed by spring (note that we don't have a controller for it)

Note:

- Spring requires a value before the password if no encryption is used: {noop}. Encoding method prefix is required for DelegatingPasswordEncoder which is default since Spring Security 5.0.0.RC1
- For authentication failure you could redirect to an error page. But I haven't done so.
- Note how security authorization is specified at path level (within the WebSecurityConfiguration) and also at method level (on the respective methods)
- Note that FailureHandler is defined as a method within WebSecurityConfiguration but SuccessHandler as a seperate class. No specific reason for this. But usually there isn't much that you do if it's a failure but more if it's success so it makes sense to have all that code in a seperate class.

Checkout the project: **securestore-web-with-db-auth-1**

## Part 2: Secure Web with In-Memory Authentication and Authorization with encryption

Same as Part 1 but we have used BCryptPasswordEncoder.

Checkout the project: **securestore-web-with-db-auth-2**

## Part 3: Secure Web with DB Authentication and Authorization with encryption

Checkout the project: **securestore-web-with-db-auth-3**

We are persisting user and role information and using that to authenticate a user. 

Note that we have create different classes. It's possible to create one single class that implements UserDetails and also domains specific attributes but to keep the code clean and not mix spring code with domain code we have different classes: CustomSecurityUser and UserAccount.

Note that we are not doing the same with Authorities bean as GrantedAuthority interface is only a marker interface.

Note that you cannot have the same name in Request mapping and Thymeleaf page, like

```java
@Controller
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping
    public String errorPage() {
        return "error";
    }
}
```

The above will fail with ambiguous mapping error for `/error`.

Checkout the project: **securestore-web-with-db-auth-3**



## Part 4: Secure Web and HTTP(Ajax) endpoints with DB Authentication and Authorization with encryption; User Registration; CSRF; Remember-Me Cookie

The same as above but with rest endpoints added. 

Checkout the project: **securestore-web-with-db-auth-4**.

Note the following changes and experiments: 

1. BeanConfigurations has been removed.
2. New Service Interface created from the spring's `UserDetailsService` to accommodate the requirements of rest endpoints
3. Added `@JsonIgnore` to getPassword method in entity to not deserialize it when User object is sent as response
4. Rest controllers can be tested from postman by adding the logged in jsession id into http headers
5. Rest endpoints are protected using `@PreAuthorize`
6. Pages endpoints changed from `/user` to `/userPage` and `/admin` to `/adminPage`
7. Default logout handler endpoint configured as a regex expression so that even if `/logout2233` is typed it works
8. Accessing profile/userdetails in a controller method (check HomeController.logUserDetails), bean (check static method in UserAccount) and also in thyme leaf page (check profile)
8. Note that for the profile page I can send the user profile data as part of page/request attributes from the controller funtion but wanted to try out built in thymeleaf facilities. But note I ended up adding a new dependency (check pom) for this.
10. Enabling Remember-Me cookie 

#### Method Security

That approach is called `*method security*` and works through annotations that you can basically put on any public method of your Spring beans. You also need to explicitly enable method security by putting the @EnableGlobalMethodSecurity annotation on your ApplicationContextConfiguration.

```java
@Configuration
@EnableGlobalMethodSecurity(
  prePostEnabled = true, // (1)
  securedEnabled = true, // (2)
  jsr250Enabled = true) // (3)
public class YourSecurityConfig extends WebSecurityConfigurerAdapter{
}
```

1. The prePostEnabled property enables support for Spring’s `*@PreAuthorize*` and `*@PostAuthorize*` annotations. Support means, that Spring will ignore this annotation unless you set the flag to true.
2. The securedEnabled property enables support for the `*@Secured*` annotation. Support means, that Spring will ignore this annotation unless you set the flag to true.
3. The jsr250Enabled property enables support for the `*@RolesAllowed*` annotation. Support means, that Spring will ignore this annotation unless you set the flag to true.

#### What is the difference between @PreAuthorize, @Secured and @RolesAllowed?

@Secured and @RolesAllowed are basically the same, though @Secured is a Spring-specific annotation coming with the spring-security-core dependency and @RolesAllowed is a standardised annotation, living in the javax.annotation-api dependency. Both annotations take in an authority/role string as value.

@PreAuthorize/@PostAuthorize are also (newer) Spring specific annotations and more powerful than the above annotations, as they can contain not only authorities/roles, but also *any* valid SpEL expression.

Lastly, all these annotations will raise an `AccessDeniedException` if you try and access a protected method with an insufficient authority/role.

#### Enabling Remember-Me cookie

The basic configuration using the *rememberMe()* method is extremely simple while remaining very flexible though additional options. The ***key*** is important here – it is a private value secret for the entire application and it will be used when generating the contents of the token.

Additionally, the time the token is valid can be configured from the default of 2 weeks to – for example – one day using *tokenValiditySeconds()*:

```java
rememberMe().key("uniqueAndSecret").tokenValiditySeconds(86400).rememberMeParameter("rememberMe")
```

If the form parameter is different ``rememberMe`from the default of` remember-me` then it needs to be configured.

How it works?

The mechanism will create an additional cookie – the “remember-me” cookie – when the user logs in.

The **Remember Me cookie** contains the following data:

- **username** – to identify the logged in principal
- **expirationTime** – to expire the cookie; default is 2 weeks
- **MD5 hash** – of the previous 2 values – *username* and *expirationTime*, plus the *password* and the predefined *key*

First thing to notice here is that both the *username* and the *password* are part of the cookie – this means that, if either is changed, the cookie is no longer valid. Also, the *username* can be read from the cookie.

Additionally, it is important to understand that this mechanism is potentially vulnerable if the remember me cookie is captured. **The cookie will be valid and usable** until it expires or the credentials are changed.

I tested it and when revisiting /userPage (after loging in and restarting the server) it takes me to the page without asking to login again. 

Also you can observe the magic happening in the logs.

```verilog
2020-08-01 14:43:03.774 DEBUG 69233 --- [nio-8085-exec-1] o.s.security.web.FilterChainProxy        : /userPage at position 8 of 12 in additional filter chain; firing Filter: 'RememberMeAuthenticationFilter'
2020-08-01 14:43:03.775 DEBUG 69233 --- [nio-8085-exec-1] o.s.s.w.a.r.TokenBasedRememberMeServices : Remember-me cookie detected
2020-08-01 14:43:03.786 DEBUG 69233 --- [nio-8085-exec-1] tor$SharedEntityManagerInvocationHandler : Creating new EntityManager for shared EntityManager invocation
Hibernate: select useraccoun0_.id as id1_1_0_, authoritie1_.id as id1_0_1_, useraccoun0_.age as age2_1_0_, useraccoun0_.name as name3_1_0_, useraccoun0_.password as password4_1_0_, useraccoun0_.username as username5_1_0_, authoritie1_.authority as authorit2_0_1_, authoritie1_.user_account_id as user_acc3_0_1_, authoritie1_.user_account_id as user_acc3_0_0__, authoritie1_.id as id1_0_0__ from user_account useraccoun0_ left outer join authorities authoritie1_ on useraccoun0_.id=authoritie1_.user_account_id where useraccoun0_.username=?
2020-08-01 14:43:03.864 DEBUG 69233 --- [nio-8085-exec-1] o.s.s.w.a.r.TokenBasedRememberMeServices : Remember-me cookie accepted
2020-08-01 14:43:03.865 DEBUG 69233 --- [nio-8085-exec-1] o.s.s.authentication.ProviderManager     : Authentication attempt using org.springframework.security.authentication.RememberMeAuthenticationProvider
2020-08-01 14:43:03.866 DEBUG 69233 --- [nio-8085-exec-1] s.s.w.a.r.RememberMeAuthenticationFilter : SecurityContextHolder populated with remember-me token: 'org.springframework.security.authentication.RememberMeAuthenticationToken@700c3d62: Principal: com.securestore.domain.CustomSecurityUser@220b8527; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: com.securestore.domain.Authorities@50637633'

```

#### Cross-Site-Request-Forgery: CSRF

If you are completely new to CSRF, you might want to watch [this YouTube video](https://www.youtube.com/watch?v=vRBihr41JTo) to get up to speed with it. 

##### CSRF & Server-Side Rendered HTML

Imagine a bank transfer form or any form (like a login form) for that matter, that gets rendered by your @Controllers with the help of a templating technology like Thymeleaf or Freemarker.

```html
<form action="/transfer" method="post">  <!-- 1 -->
  <input type="text" name="amount"/>
  <input type="text" name="routingNumber"/>
  <input type="text" name="account"/>
  <input type="submit" value="Transfer"/>
</form>
```

With Spring Security enabled, you **won’t be able to submit that form anymore**. 

Because Spring Security’s CSRFFilter is looking for an *additional hidden parameter* on **any POST** (PUT/DELETE) request: a so-called CSRF token.

It generates such a token, by default, *per HTTP session* and stores it there. And you need to make sure to inject it into any of your HTML forms.

#### CSRF Tokens & Thymeleaf

As Thymeleaf has good integration with Spring Security (when used together with Spring Boot), you can simply add the following snippet to any form and you’ll get the token injected automatically, from the session, into your form. Even better, if you are using "th:action" for your form, Thymeleaf will *automatically* inject that hidden field for you, without having to do it manually.

```html
<form action="/transfer" method="post">  <!-- 1 -->
  <input type="text" name="amount"/>
  <input type="text" name="routingNumber"/>
  <input type="text" name="account"/>
  <input type="submit" value="Transfer"/>
  <input 
  type="hidden" 
  th:name="${_csrf.parameterName}" 
  th:value="${_csrf.token}" />
</form>

<!-- OR -->

<form th:action="/transfer" method="post">  <!-- 2 -->
  <input type="text" name="amount"/>
  <input type="text" name="routingNumber"/>
  <input type="text" name="account"/>
  <input type="submit" value="Transfer"/>
</form>
```

1. Here, we are adding the CSRF parameter manually.
2. Here, we are using Thymeleaf’s form support.

Note that I have enabled it in this project alone. Enabling it is as simple as removing the `.csrf().disable();` line.

CSRF is not needed for stateless session setup like token authorisation (eg.  JWT). It can be disabled.

For testing CSRF have a look [here](https://www.baeldung.com/spring-security-csrf).

#### User Registration

This is straight forward. Adding the register form and register endpoints made it happen.

For adding more custom password validations: https://stackabuse.com/spring-custom-password-validation/

Checkout the project: **securestore-web-with-db-auth-4**.



## Part 5: Custom authentication; Custom Encryption; Session Management

Checkout the project: **securestore-web-with-db-auth-5**.

#### Adding a custom Authentication Provider for custom authentication

Note that till now we used the Approach (1) for Authentication which is the default provided by Spring. Now we are going to try Approach (2)

Essentially what happened is spring provided an implementation for `AuthenticationProvider` called the *DaoAuthenticationProvider* – which retrieves the user details from a simple, read-only user DAO – the *UserDetailsService*. 

This User Details Service **only has access to the username** in order to retrieve the full user entity. This is enough for most scenarios.

More custom scenarios will still need to access the full *Authentication* request to be able to perform the authentication process. For example, when authenticating against some external, third party service (such as [Crowd](https://www.atlassian.com/software/crowd)) – **both the username and the password from the authentication request will be necessary**.

Let's take a s<u>impler usecase of having to use one more field</u> called `domain` while authenticating a user.

There are 2 ways we could achieve this 

1. Naive and Easy approach using a Servlet Filter
2. Advanced approach

Both approaches are explained: https://www.baeldung.com/spring-security-extra-login-fields

Let's try the advanced approach.

We would need few more classes and modifications on existing ones.

- **CustomAuthenticationFilter –** an extension of *UsernamePasswordAuthenticationFilter*
- **UserAccountAuthenticationProvider –** an extension of *AbstractUserDetailsAuthenticationProvider*
- **CustomAuthenticationToken –** an extension of *UsernamePasswordAuthenticationToken*

Will require modifications on 

- **UserAccountDetailsService and UserAccountDetailsServiceImpl** to add loadUserByUsernameAndDomain since we won't be using loadUserByUsername

- **WebSecurityConfiguration** to make changes to `protected void configure(AuthenticationManagerBuilder auth)` to not use userDetailsService and instead a custom authentication provider. This is the place where we switch from Approach 1 to Approach 2.

Note that spring internally uses auth tokens in all approaches. Even in the default and out-of-the-box code it uses and transfers the username and password from the front end into *UsernamePasswordAuthenticationToken*.

Let's not get confused with the "token" terminology.

#### Session Management

   We like to avoid creating unwanted sessions and only when required. Aloso would like to disallow same login sessions.

   Changes are required only in **WebSecurityConfiguration**.

#### Custom Encryption

   A custom PasswordEncryption class is created by implementing PasswordEncoder.

   Though I wrote the code I didn't use it in  WebSecurityConfiguration since it might make the existing credentials invalid.

#### Logout Handler and Caching

   When **a user attempts a logout, it has several consequences on its current session state**. We need to destroy the session with two steps:

   1. Invalidate HTTP session information.
   2. Clear *SecurityContext* as it contains authentication information.

   Those two actions are performed by the *SecurityContextLogoutHandler.*

   Let's see that in action:

   ```java
   @Override
   protected void configure(HttpSecurity http) throws Exception {
     http
       .logout(logout -> logout
               .logoutUrl("/basic/basiclogout")
               .addLogoutHandler(new SecurityContextLogoutHandler())
              );
   }
   ```

   Note that *SecurityContextLogoutHandler* is added by Spring Security by default – we just show it here for clarity.  

   The default handler takes care of clearing security context but to remove the cookies or cache on the client side we have to be explicit.

   Spring provides a cookie clearing handler called *CookieClearingLogoutHandler*.

   Note that we used

   `deleteCookies("JSESSIONID")`

   this allows specifying the names of cookies to be removed on logout success. This is a shortcut to easily invoke addLogoutHandler(LogoutHandler) with a CookieClearingLogoutHandler.

   Alternate approach is

   ```java
   .addLogoutHandler((request, response, auth) -> {
     for (Cookie cookie : request.getCookies()) {
       String cookieName = cookie.getName();
       Cookie cookieToDelete = new Cookie(cookieName, null);
       cookieToDelete.setMaxAge(0);
       response.addCookie(cookieToDelete);
     }
   })
   ```

   

   Sometimes we might need to clear the cached data also.

   Just like the *[Cache-Control](https://www.baeldung.com/spring-mvc-cache-headers)* header, *Clear-Site-Data* is an HTTP response header. Websites can use this header to instruct browsers to remove the data cached in local storage.

   For websites that require authentication, the *Cache-Control* header is generally included in the */login* response and allows browsers to cache user data. Similarly, websites include the *Clear-Site-Data* header in the */logout* response to clear any cached data that belongs to this user.

   At this point, it's important to understand that browsers usually categorize local storage into different types:

   - Local Storage
   - Session Storage
   - Cookies

   **Since websites can store data in any one of these types, Clear-Site-Data allows us to specify the target storage in the header:**

   - *cache* – to remove locally cached data and includes both private and shared browser caches
   - *cookies* – to remove data stored in browser cookies
   - *storage* – to clear local and session storage of the browser
   - *executionContexts* – this switch tells the browser to reload the browser tab for that URL
   - *** (asterisk) – removes data from all of the above storage areas

   As a result, the *Clear-Site-Data* header must include at least one of these storage types:

   ```plaintext
   Clear-Site-Data: "cache", "cookies", "storage", "executionContexts"
   ```

   Spring Security provides a *ClearSiteDataHeaderWriter* class to add the header in the HTTP response easily:

   ```java
   .logout().logoutUrl("/logout")
     .addLogoutHandler(new HeaderWriterLogoutHandler(
       new ClearSiteDataHeaderWriter(
         ClearSiteDataHeaderWriter.Directive.CACHE,
         ClearSiteDataHeaderWriter.Directive.COOKIES,
         ClearSiteDataHeaderWriter.Directive.STORAGE)));
   ```

   Now upon hitting the /logout the browser will receive `Clear-Site-Data: "cache", "cookies", "storage"` as part of Response header.

   Note that I didn't specify cache setting for static resources nor for the controller responses. I left it with the defaults.

   Check this to know more about it: https://www.baeldung.com/spring-mvc-cache-headers

#### Remember Me cookie when custom authentication  

> Getting the exisitng Remember Me cookie to work with custon authentication In progress. Note that this feature alone won't work in  **securestore-web-with-db-auth-4**.

I have raised this as a [stackoverflow question](https://stackoverflow.com/questions/63325459/spring-remember-me-cookie-doesnt-work-for-custom-authentication-provider). If you want Remember Me cookie with more security then go for Persisted Spring Security have a look here: https://github.com/eugenp/tutorials/tree/master/spring-security-modules/spring-security-mvc-persisted-remember-me

Checkout the project: **securestore-web-with-db-auth-5**.



## Part 6:  Secure REST (Stateless Sessions)

Checkout the project: **securestore-rest-with-db-auth-6**.

There are multiple options to choose from:

1. Basic - the simplest with least features

2. Digest - exotic, complex but not very widely used now (([spring-digest-impl-sample](https://github.com/sumit-samaddar/spring-rest-with-digest-authentication)))

3. Bearer tokens - some custom implementation or JWT

A good comparison post is [here](https://www.securitydrops.com/the-web-api-authentication-guide/)

### Stateful to Stateless : Simple approach : Custom non-standard token

If you already have a web app based on Stateful sessions then most obvious idea would be to send our session object’s content **back to the client** and have them stored in, say, a cookie. They would of course be **encrypted by the server** before sending, so that no prying eyes would be able to read them.
When querying the server, the client would send that encrypted cookie along with the intended request. The server would then decrypt the cookie’s content and consequently verify the obtained credentials. Since the server had previously encrypted the cookie’s content using its own **secret key**, we are pretty sure that the credentials obtained are authentic unless the secret key is leaked, that is.

This approach has been made pretty much straightforward thanks to contributions [like this one](https://github.com/lawrence0819/java-stateless-http-session), which allows you to intercept the session’s content, encrypt it and send it to the browser as a cookie.

### Stateful to Stateless : Complex approach : JWT token

JWT is nothing is mostly same as above but in a standardized approach.

**JWT** (JSON Web Tokens) is the **open standard** version of that concept: a **JSON-based** solution for creating secure tokens that contain a certain amount of **claims**, such as “I am indeed logged”, “I have administration rights”, etc. 

These tokens are standard, compact, **encrypted** and **signed** by the emitting entity so that whoever receives them can ensure they are legitimate.

As such, they can be perfectly used for authenticating a web or mobile application to a server exposing a RESTful API.

One of the major advantages of JWT compared to session content’s serialization is that it can be used by any language or technology that can handle JSON, including Java, .NET, JavaScript, Python.

 Just point your browser to https://jwt.io/ and have a look at the amount and variety of libraries available to generate, sign and verify JWT tokens! 

### JWT

JWTs (pronounced “jots”) are URL-safe, encoded, cryptographically signed (sometimes encrypted) strings that can be used as tokens in a variety of applications.  JWT can beused as a CSRF too. The following explanation from [baeldung](https://www.baeldung.com/).

#### Structure of JWT

You may have noticed that there are three distinct sections separated by periods (`.`):

`eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJlNjc4ZjIzMzQ3ZTM0MTBkYjdlNjg3Njc4MjNiMmQ3MCIsImlhdCI6MTQ2NjYzMzMxNywibmJmIjoxNDY2NjMzMzE3LCJleHAiOjE0NjY2MzY5MTd9.rgx_o8VQGuDa2AqCHSgVOD5G68Ld_YYM7N7THmvLIKc`

| Section   | Value                                                        |
| --------- | ------------------------------------------------------------ |
| Header    | eyJhbGciOiJIUzI1NiJ9                                         |
| Payload   | eyJqdGkiOiJlNjc4ZjIzMzQ3ZTM0MTBkYjdlNjg3Njc4MjNiMmQ3MCIsImlhdC<br/>I6MTQ2NjYzMzMxNywibmJmIjoxNDY2NjMzMzE3LCJleHAiOjE0NjY2MzY5MTd9 |
| Signature | rgx_o8VQGuDa2AqCHSgVOD5G68Ld_YYM7N7THmvLIKc                  |

Each section is [base64](https://en.wikipedia.org/wiki/Base64) URL-encoded. This ensures that it can be used safely in a URL (more on this later). Let's take a closer look at each section individually.

##### **2.1. The Header**

If you base64 to decode the header, you will get the following JSON string:

```javascript
{"alg":"HS256"}
```

This shows that the JWT was signed with [HMAC](https://en.wikipedia.org/wiki/Hash-based_message_authentication_code) using [SHA-256](https://en.wikipedia.org/wiki/SHA-2).

##### **2.2. The Payload**

If you decode the payload, you get the following JSON string (formatted for clarity):

```json
{
  "jti": "e678f23347e3410db7e68767823b2d70",
  "iat": 1466633317,
  "nbf": 1466633317,
  "exp": 1466636917
}
```

Within the payload, as you can see, there are a number of keys with values. These keys are called “claims” and the [JWT specification](https://tools.ietf.org/html/rfc7519#section-4.1) has seven of these specified as “registered” claims. They are:

| iss  | Issuer     |
| ---- | ---------- |
| sub  | Subject    |
| aud  | Audience   |
| exp  | Expiration |
| nbf  | Not Before |
| iat  | Issued At  |
| jti  | JWT ID     |

When building a JWT, you can put in any custom claims you wish. The list above simply represents the claims that are reserved both in the key that is used and the expected type. The expiration time is exactly one minute past the issued at time.

##### **2.3. The Signature**

Finally, the signature section is created by taking the header and payload together (with the . in between) and passing it through the specified algorithm (HMAC using SHA-256, in this case) along with a known secret. Note that the secret is *always* a byte array, and should be of a length that makes sense for the algorithm used. Below, I use a random base64 encoded string (for readability) that's converted into a byte array.

It looks like this in pseudo-code:

```javascript
computeHMACSHA256(
    header + "." + payload, 
    base64DecodeToByteArray("4pE8z3PBoHjnV1AhvGk+e8h2p+ShZpOnpr8cwHmMh1w=")
)
```

As long as you know the secret, you can generate the signature yourself and compare your result to the signature section of the JWT to verify that it has not been tampered with. Technically, a JWT that's been cryptographically signed is called a [JWS](https://tools.ietf.org/html/rfc7515). JWTs can also be encrypted and would then be called a [JWE](https://tools.ietf.org/html/rfc7516). (In actual practice, the term JWT is used to describe JWEs and JWSs.)

https://www.baeldung.com/java-json-web-tokens-jjwt

There were few issues to get the code working

1. the secret key size was below 256 bits

   HMAC256 requires key size to be equal or greater. Most examples online shows secret as a few character length word - which is incorrect and unsafe.

2. some of the methods were deprecated, like `JwtBuilder signWith(SignatureAlgorithm var1, String var2);`

   I changed my code to use a standard approach.

Both the points are discussed [here](https://stackoverflow.com/questions/40252903/static-secret-as-byte-key-or-string). 

Note: 

When using JWT token for **microservice authentication/authorization** it is advisable to sign with RSA Private/Public Keys instead of using Shared HMAC Secret. The token is generated and signed by a central authority (usually an Authorization Server) and each microservice can validate the JWT token using the Public Key exposed from Authorization Server.

```sh
$ openssl genrsa -out key.pem 2048
Generating RSA private key, 2048 bit long modulus
..........+++
..........................................................................+++
e is 65537 (0x10001)
```

```sh
$ openssl rsa -in key.pem -outform PEM -pubout -out public.pem
writing RSA key
```

The key.pem file contains the private key generated using RSA and public.pem file contains public key.

And in code you will essentially do `.signWith(privateKey)`

The private key generated as follows:

```java
String rsaPrivateKey = "-----BEGIN PRIVATE KEY-----" +
  "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDK7c0HtOvefMRM" +
  "s1tkdiJm+A16Df85lQlmXjQvMHNgY4P/znvl4kRON9DdBdo3K81OG7pR/0H9XvdB" +
  ....
  "NdPhTRVIAWhil7rFWANOO3Gw2KrMy6O1erW7sAjQlZECgYBmjXWzgasT7JcHrP72" +
  "fqrEx4cg/jQFNlqODNb515tfXSBBoAFiaxWJK3Uh/60/I6cFL/Qoner4trNDWSNo" +
  "YENBqXLZnWGfIo0vAIgniJ6OD67+1hEQtbenhSfeE8Hou2BnFOTajUxmYgGm3+hx" +
  "h8TPOvfHATdiwIm7Qu76gHhpzQ==" +
  "-----END PRIVATE KEY-----";

rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
rsaPrivateKey = rsaPrivateKey.replace("-----END PRIVATE KEY-----", "");

PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
KeyFactory kf = KeyFactory.getInstance("RSA");
PrivateKey privateKey = kf.generatePrivate(keySpec);
```

To validate you then do `.setSigningKey(publicKey)`

```java
String rsaPublicKey = "-----BEGIN PUBLIC KEY-----" +
  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyu3NB7Tr3nzETLNbZHYi" +
  "ZvgNeg3/OZUJZl40LzBzYGOD/8575eJETjfQ3QXaNyvNThu6Uf9B/V73QUxKI4/+" +
  "rwlbjA3niIga4MdDiY4b9K/KFA+HedvtZF1yE2p4smXGydPLOLBe31EgriGTob78" +
  "EE3f7SMFxlNaqn4Pm7KJkOodnMz0ilwLseeL1IkTtiFn/2OrcMpPHMtTxyDn3pQl" +
  "VCeJM5j/grDh+0YdyTMGdDHOBgM53VqSsDVyo1TNtP2yhPRYCIiI85hEHVaUnVM9" +
  "jGwCjNZLJHWh10Mrmh6B3z8BEmLhMAZXeL4fQBjBd42DLvIIJwM1USKFhjK+XghN" +
  "rQIDAQAB" +
  "-----END PUBLIC KEY-----";
rsaPublicKey = rsaPublicKey.replace("-----BEGIN PUBLIC KEY-----", "");
rsaPublicKey = rsaPublicKey.replace("-----END PUBLIC KEY-----", "");
X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKey));
KeyFactory kf = KeyFactory.getInstance("RSA");
PublicKey publicKey = kf.generatePublic(keySpec);
return publicKey;
}
```

Checkout the project: **securestore-rest-with-db-auth-6**.



## Part 7: Secure OAuth (DB Authentication and Authorization with encryption)

### OAuth

The [OAuth 2.0 specification](http://tools.ietf.org/html/rfc6749) is a flexibile authorization framework that describes a number of grants (“methods”) for a client application to acquire an access token (which represents a user’s permission for the client to access their data) which can be used to authenticate a request to an API endpoint.

The specification describes five grants for acquiring an access token:

- Authorization code grant
- Implicit grant
- Resource owner credentials (or Password) grant
- Client credentials grant
- Refresh token grant

OAuth terms (taken from the core spec):

- **Resource owner (a.k.a. the User)** - An entity capable of granting access to a protected resource. When the resource owner is a person, it is referred to as an end-user.
- **Resource server (a.k.a. the API server)** - The server hosting the protected resources, capable of accepting and responding to protected resource requests using access tokens. (In case of Oauth with google this is the google server hosting user information like name, email, photo, etc)
- **Client** - An application making protected resource requests on behalf of the resource owner and with its authorization. The term client does not imply any particular implementation characteristics (e.g. whether the application executes on a server, a desktop, or other devices).
- **Authorization server** - The server issuing access tokens to the client after successfully authenticating the resource owner and obtaining authorization.

Below explanation from [DZone](https://dzone.com/articles/an-oauth2-grant-selection-decision-tree-for-securi) and [AlexBilbie](https://alexbilbie.com/tag/oauth/)

#### Abstract Protocol Overview

![img](./1*xcp97z1I3jHUSFtv4nKtYg.png)

#### Authorisation Code Grant 

The authorization code grant should be very familiar if you’ve ever signed into an application using your Facebook or Google account.

![img](./1*GFnUVr2IHn7XoW9-wOF1zQ.png)



##### **Step A: Authorization Request**

First, the client application makes an authorization request by redirecting the user to the authorization server by specifying the 

- `response_type` with the value `code`
- `client_id` with the client identifier
- `redirect_uri` with the client redirect URI. This parameter is optional, but if not send the user will be redirected to a pre-registered redirect URI.
- `scope` a space delimited list of scopes
- `state` with a [CSRF](https://en.wikipedia.org/wiki/Cross-site_request_forgery) token. This parameter is optional but highly recommended. You should store the value of the CSRF token in the user’s session to be validated when they return.

```bash
GET /authorize?response_type=code         
&client_id=s6BhdRkqt3&state=xyz         
&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb HTTP/1.1
Host: server.example.com
```

##### **Step B: User Authentication**

Then, the user will be prompted to log in, either on the authorization server itself or by federating to a third party identity provider.

##### **Step C: Authorization Response**

If the user approves the client they will be redirected from the authorisation server back to the client (specifically to the redirect URI) with the following parameters in the query string:

- `code` with the authorization code
- `state` with the state parameter sent in the original request. You should compare this value with the value stored in the user’s session to ensure the authorization code obtained is in response to requests made by this client rather than another client application.

```bash
HTTP/1.1 302 Found
Location: https://client.example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA               
&state=xyz
```

##### **Step D: Access Token Request**

Afterwards (any other point in time; could be immediately or later), the client application will make another request to the authorization server to obtain an access token using the given authorization code.

POST request to the authorization server with the following parameters:

- `grant_type` with the value of `authorization_code`
- `client_id` with the client identifier
- `client_secret` with the client secret
- `redirect_uri` with the same redirect URI the user was redirect back to
- `code` with the authorization code from the query string

```bash
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded
grant_type=authorization_code     
&code=SplxlOBeZQQYbYS6WxSbIA     
&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
```

##### **Step E: Access Token Response**

If the token request is valid, the authorization server will respond with a JSON object containing the following properties:

- `token_type` this will usually be the word “Bearer” (to indicate a bearer token)
- `expires_in` with an integer representing the TTL of the access token (i.e. when the token will expire)
- `access_token` the access token itself
- `refresh_token` a refresh token that can be used to acquire a new access token when the original expires

```bash
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache
{   "access_token":"2YotnFZFEjr1zCsicMWpAA",   
    "token_type":"example",   
    "expires_in":3600,   
    "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",   
    "example_parameter":"example_value"
}
```

##### **Step F: Resource Request**

Thereafter, the client application will use the access token for requesting resources hosted on the resource server by adding the authorization header.

```bash
GET /example/resource HTTP/1.1
Host: server.example.com
Authorization: Bearer 2YotnFZFEjr1zCsicMWpAA
```



#### Client Credentials Grant

![](./1_jN4zePKhl9C34tLeVcIdyw.png)

The Client Credentials Grant might be the simplest grant in OAuth2 specification. It has been designed to be used for machine to machine communication where the end user identification is not necessary for resource authorization. 

In this approach, the client application itself acts as the resource owner and make use of the client credentials for obtaining access tokens:

##### **Step A: Authentication Request**

First, the client application makes an authorization request to the authorization server by providing the client credentials in the Authorization header and the grant type in the message body:

```bash
POST /token HTTP/1.1     
Host: server.example.com     
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW     
Content-Type: application/x-www-form-urlencoded
grant_type=client_credentials
```

##### **Step B: Authentication Response**

If the authentication request is successful, the authorization server will respond with an access token, access token expiration time, and any other application specific parameters:

```bash
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache
{   "access_token":"2YotnFZFEjr1zCsicMWpAA",   
    "token_type":"example",   
    "expires_in":3600,   
    "example_parameter":"example_value"
}
```

##### **Step C: Resource Request:**

Thereafter, the client application will use the access token for requesting resources hosted on the resource server:

```bash
GET /example/resource HTTP/1.1
Host: server.example.com
Authorization: Bearer 2YotnFZFEjr1zCsicMWpAA
```



#### Implicit Grant

![1_X11NuSKmSn3pui6-aiyLWA](./1_X11NuSKmSn3pui6-aiyLWA.png)

The Implicit Grant has been designed for client applications such as client-side web applications that download its code and executes on a web browser which cannot securely store its client credentials. Unlike the Authorization Code Grant, this does not use client credentials for authenticating the client application. Instead, it relies on one the resource owner authentication for providing the access token.

##### **Step A: Authorization Request**

First, the client application will make an authorization request to the authorization server by specifying the response type, client id, state (an opaque value such as a CSRF token for preventing cross-site request forgery attacks) and the redirection URL:

```bash
GET /authorize?response_type=token        
&client_id=s6BhdRkqt3        
&state=xyz        
&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb HTTP/1.1
Host: server.example.com
```

##### **Step B: User Authentication**

Then, the user will be prompted to log in, either on the authorization server itself or by federating to a third party identity provider.

##### **Step C: Access Token Response**

Once the authentication is successful, the authorization server will respond with an access token, state, token type, and token expiration time:

```bash
HTTP/1.1 302 Found
Location: http://example.com/cb#access_token=2YotnFZFEjr1zCsicMWpAA               
&state=xyz&token_type=example&expires_in=3600
```

##### **Step D: Resource Request**

Thereafter, the client application will use the access token for requesting resources hosted on the resource server:

```bash
GET /example/resource HTTP/1.1
Host: server.example.com
Authorization: Bearer 2YotnFZFEjr1zCsicMWpAA
```

#### Resource owner credentials grant

This grant is a great user experience for trusted first party clients both on the web and in native device applications.

![img](./1*X6M2rK7MqALMOKjuqqsueg.png)

##### **Step A: Authentication Request**

First, the client application will make an authentication request to the authorization server by providing the 

- `grant_type` with the value `password`
- `client_id` with the the client’s ID 
- `client_secret` with the client’s secret
- `scope` with a space-delimited list of requested scope permissions.
- `username` with the user’s username
- `password` with the user’s password

```bash
POST /token HTTP/1.1
Host: server.example.com
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
Content-Type: application/x-www-form-urlencoded
grant_type=password&username=johndoe&password=A3ddj3w
```

##### **Step B: Access Token Response**

The authorization server will respond with a JSON object containing the following properties:

- `token_type` with the value `Bearer`
- `expires_in` with an integer representing the TTL of the access token
- `access_token` the access token itself
- `refresh_token` a refresh token that can be used to acquire a new access token when the original expires

```bash
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache
{   "access_token":"2YotnFZFEjr1zCsicMWpAA",   
    "token_type":"example",   
    "expires_in":3600,   
    "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",   
    "example_parameter":"example_value"
}
```

##### **Step C: Resource Request**

Thereafter, the client application will use the access token for requesting resources hosted on the resource server by specifying it in the Authorization header.

```bash
GET /example/resource HTTP/1.1
Host: server.example.com
Authorization: Bearer 2YotnFZFEjr1zCsicMWpAA
```



### Refresh Token Workflow

Once an access token is obtained using one of the above grants, it has been designed to expire after the given token expiration time for providing additional security. Due to this design, the client application will need to use the refresh token for obtaining a new token when the existing token expires. Even though this can be implemented by using a timer on the client application for obtaining a new token when the expiration time occurs, the best practice is to make an additional call to obtain a new token when the authorization server responds with the invalid token error.

![1_DU-2Xafpyun8xNFLTfsdIw](./1_DU-2Xafpyun8xNFLTfsdIw.png)

The following is a sample HTTP POST request for obtaining a new access token using the refresh token:

```bash
POST /token HTTP/1.1
Host: server.example.com 
grant_type=refresh_token
&client_id=3MVG9lKcPoNINVBIPJjdw1J9LLM82HnFVV
&client_secret=12312342342wefsdfsf3241334
&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA
```

### Grant Selection Decision Tree

According to the OAuth2 specification, the grant selection for securing an API using OAuth2 can be taken based on the requirements for end user identification, client type (server-side web application, native application, client-side web application) and the level of trust the resource owner would have on the client application:

![1_gbLB8x90pMPRo3UWbx5wug](./1_gbLB8x90pMPRo3UWbx5wug.png)



If the resource server does not require identifying the end user who interacts with the client application, and if the client application itself acts as the resource owner it can use the Client Credentials Grant. In this approach, the client application only requires client credentials for issuing access tokens and it does not keep track of the end user. This approach would be suitable for server-to-server communication.

If the end user identification is needed in the resource server for authorization, and if the client is either a server-side web application or a native application accessed by third party users, the OAuth2 specification recommends using the Authorization Code Grant. This is the most recommended grant for securing resources accessed by publicly hosted applications and third-party users due to its design of exchanging authorization codes for access tokens. 

If the end user identification is needed in the resource server for authorization, and if the client application is either a client-side web application or a native mobile or desktop application that is used by the first party, the Resource Owner Password Credentials Grant can be used for obtaining access tokens. In this workflow, both user agent based and native applications do not require-to-store client credentials on their applications. Instead, it makes use of the end user credentials for obtaining the access tokens for increased security. Moreover, it is important to note the requirement of the trust between the resource owners and the client, because users would need to provide their user credentials in the client application itself.

If providing user credentials directly to the client application is a concern and if the authorization is done via a third-party authorization server, the Implicit Grant can be used for client-side web applications which require end-user identification. It will make use of browser redirection to navigate the user to the authorization server and obtain an access token via the response flow.



For the usecase that we are working on. Let's consider

> **Work in progress. Ignore the below notes.**

We are persisting user and role information and using that to authenticate a user. 

Note that we have create different classes. It's possible to create one single class that implements UserDetails and also domains specific attributes but to keep the code clean and not mix spring code with domain code we have different classes: CustomSecurityUser and UserAccount.

Note that we are not doing the same with Authorities bean as GrantedAuthority interface is only a marker interface.

All about password encoders:

https://dzone.com/articles/spring-security-with-spring-boot-20-password-encod

```bash
curl -X POST --user my-client:my-secret localhost:8085/securestore/oauth/token -d 'grant_type=password&username=user1&password=user1Pass' -H "Accept: application/json"
```



```bash
curl -v http://localhost:8085/securestore/api/hello -H "Authorization: Bearer 81a4c5f4-8252-401d-a522-13a2e0e20752"
```


Infinite JSON recursion

- Use  `@JsonBackReference` and `@JsonManagedReference `Correctly.
- Try moving `@JsonIgnore` from private field to **getter of the field**, 



Controller and handlers not getting registered correctly

- Make sure you annotate correctly
- `@Controller('/rest')` is incorrect. This ends up naming the controller runtime bean as /rest. Use `@RequestMapping`





References and Links:

Stackoverflow, https://www.baeldung.com/

https://www.oauth.com/oauth2-servers/accessing-data/

This guy talks about Password Grant which can be done using postman (and the same worked for me)

https://www.youtube.com/watch?v=996OiexHze0

To run both as separate service:

https://medium.com/@supunbhagya/spring-oauth2-resourceserver-oauth2-security-authorization-code-grant-flow-9eb72fd5d27d


https://github.com/teemodevs
https://shekhargulati.com/2018/02/15/single-sign-on-in-spring-boot-applications-with-spring-security-oauth/
https://alexbilbie.com/guide-to-oauth-2-grants/
https://tools.ietf.org/html/rfc6749#section-4.1.1
https://projects.spring.io/spring-security-oauth/docs/oauth2.html
http://www.zakariaamine.com/2018-03-01/using-oauth2-in-spring-scopes
https://medium.com/securing/what-is-going-on-with-oauth-2-0-and-why-you-should-not-use-it-for-authentication-5f47597b2611
https://github.com/patternmatch/oauth2-blog/tags
https://github.com/HeshamOsman/spring-oauth-sso
https://github.com/hkurosu/oauth2-sso-samples
https://github.com/making/oauth2-sso-demo
https://dzone.com/articles/spring-boot-oauth2-getting-the-authorization-code

