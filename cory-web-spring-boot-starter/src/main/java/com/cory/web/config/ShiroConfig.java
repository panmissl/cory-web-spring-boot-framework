package com.cory.web.config;

import com.cory.constant.Constants;
import com.cory.service.UserService;
import com.cory.util.MapBuilder;
import com.cory.web.security.*;
import com.cory.web.util.PasswordEncoder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import javax.servlet.Filter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Cory on 2021/2/15.
 */
@Configuration
public class ShiroConfig {

    private static final String LOGIN_HANDLE_URL = "/doLogin";
    private static final String USERNAME_PARAM = "logonId";
    private static final String REMEMBERME_PARAM = "rememberMe";
    private static final String LOGIN_URL = "/login";
    private static final String SUCCESS_URL = "/";
    private static final String UNAUTHORIZED_URL = "/errorPage?type=403";

    @Bean
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager,
                                                         AuthenticationFilter authcFilter,
                                                         UserFilter userFilter,
                                                         LogoutFilter logoutFilter,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
        bean.setLoginUrl(LOGIN_URL);
        bean.setSuccessUrl(SUCCESS_URL);
        bean.setUnauthorizedUrl(UNAUTHORIZED_URL);
        bean.setFilters(MapBuilder.create(String.class, Filter.class).put("authc", authcFilter).put("user", userFilter).put("logout", logoutFilter).build());
        bean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());
        return bean;
    }

    @Bean
    public AntPermissionResolver antPermissionResolver() {
        return new AntPermissionResolver();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new PasswordEncoder();
        encoder.setSalt("!@0#$1%^2*()");
        return encoder;
    }

    @Bean
    public CredentialsMatcher credentialsMatcher(PasswordEncoder passwordEncoder) {
        CredentialsMatcher matcher = new CredentialsMatcher();
        matcher.setPasswordEncoder(passwordEncoder);
        return matcher;
    }

    @Bean
    public Realm realm(CredentialsMatcher credentialsMatcher, AntPermissionResolver antPermissionResolver, UserService userService) {
        AuthorizingRealm realm = new AuthorizingRealm();
        realm.setPermissionResolver(antPermissionResolver);
        realm.setCredentialsMatcher(credentialsMatcher);
        realm.setAuthenticationCachingEnabled(true);
        realm.setUserService(userService);
        return realm;
    }

    @Bean
    protected CacheManager shiroCacheManager(JedisConnectionFactory connectionFactory) {
        return new ShiroRedisCacheManager(connectionFactory);
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        chainDefinition.addPathDefinition("/ajax/**", "user");

        chainDefinition.addPathDefinition("/doLogin*", "authc");
        chainDefinition.addPathDefinition("/logout*", "logout");

        chainDefinition.addPathDefinition("/doRegister*", "anon");
        chainDefinition.addPathDefinition("/login*", "anon");
        chainDefinition.addPathDefinition("/error*", "anon");
        chainDefinition.addPathDefinition("/currentUser", "anon");
        chainDefinition.addPathDefinition("/generateCsrfToken", "anon");
        chainDefinition.addPathDefinition("/generateFormToken", "anon");
        chainDefinition.addPathDefinition("/openapi/**", "anon");

        chainDefinition.addPathDefinition("/**", "user");
        return chainDefinition;
    }

    @Bean
    public AuthenticationFilter authcFilter() {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.setLoginHandleUrl(LOGIN_HANDLE_URL);
        filter.setUsernameParam(USERNAME_PARAM);
        filter.setRememberMeParam(REMEMBERME_PARAM);
        filter.setLoginUrl(LOGIN_URL);
        filter.setSuccessUrl(SUCCESS_URL);
        return filter;
    }

    @Bean
    public UserFilter userFilter() {
        return new UserFilter();
    }

    @Bean
    public LogoutFilter logoutFilter() {
        return new LogoutFilter();
    }

    @Bean
    public FilterRegistrationBean authcRegistration(AuthenticationFilter authcFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(authcFilter);
        // 该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean userRegistration(UserFilter userFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(userFilter);
        // 该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean logoutRegistration(LogoutFilter logoutFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(logoutFilter);
        // 该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        registration.setEnabled(false);
        return registration;
    }

        @Bean
    public DefaultWebSecurityManager securityManager(CacheManager cacheManager, CookieRememberMeManager rememberMeManager, Realm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRememberMeManager(rememberMeManager);
        manager.setRealm(realm);
        manager.setCacheManager(cacheManager);

        SecurityUtils.setSecurityManager(manager);

        return manager;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public SimpleCookie sessionIdCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("sid");
        simpleCookie.setMaxAge(-1);
        simpleCookie.setHttpOnly(true);

        return simpleCookie;
    }

    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //30 days
        simpleCookie.setMaxAge(2592000);
        simpleCookie.setHttpOnly(true);

        return simpleCookie;
    }

    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        try {
            manager.setCipherKey("cory_cookie_key".getBytes(Constants.UTF8));
        } catch (UnsupportedEncodingException e) {}
        manager.setCookie(rememberMeCookie);
        return manager;
    }
}
