package com.cory.config;

import com.cory.constant.Constants;
import com.cory.web.security.*;
import com.cory.web.util.PasswordEncoder;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by Cory on 2021/2/15.
 */
@Configuration
public class ShiroConfig {

    /*
    @Bean
    public Object _test_shiroFilterFactoryBeanPostProcessor(ShiroFilterFactoryBean shiroFilterFactoryBean) {
        shiroFilterFactoryBean.setUnauthorizedUrl("error?type=403");
        return new Object();
    }
    */

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
    public Realm realm(CredentialsMatcher credentialsMatcher, AntPermissionResolver antPermissionResolver) {
        AuthorizingRealm realm = new AuthorizingRealm();
        realm.setPermissionResolver(antPermissionResolver);
        realm.setCredentialsMatcher(credentialsMatcher);
        realm.setAuthenticationCachingEnabled(true);
        return realm;
    }

    @Bean
    protected CacheManager shiroCacheManager(JedisConnectionFactory connectionFactory) {
        return new ShiroRedisCacheManager(connectionFactory);
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        chainDefinition.addPathDefinition("/doLogin*", "authc");
        chainDefinition.addPathDefinition("/doRegister*", "anon");
        chainDefinition.addPathDefinition("/logout*", "logout");
        chainDefinition.addPathDefinition("/login*", "anon");
        chainDefinition.addPathDefinition("/error*", "anon");

        chainDefinition.addPathDefinition("/ajax/**", "user");
        chainDefinition.addPathDefinition("/", "user");

        chainDefinition.addPathDefinition("/**", "anon");
        return chainDefinition;
    }

    @Bean
    public AuthenticationFilter authcFilter() {
        AuthenticationFilter filter = new AuthenticationFilter();
        filter.setLoginHandleUrl("/doLogin");
        filter.setUsernameParam("logonId");
        filter.setRememberMeParam("rememberMe");
        filter.setLoginUrl("/login");
        filter.setSuccessUrl("/");
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
    public DefaultWebSecurityManager securityManager(CacheManager cacheManager, CookieRememberMeManager rememberMeManager, Realm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRememberMeManager(rememberMeManager);
        manager.setRealm(realm);
        manager.setCacheManager(cacheManager);

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
