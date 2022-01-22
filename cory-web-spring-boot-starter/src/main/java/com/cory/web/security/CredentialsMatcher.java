package com.cory.web.security;

import com.cory.web.util.PasswordEncoder;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Cory on 2017/5/21.
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        Object tokenHashedCredentials = token.getCredentials();
        String tokenCredentials;
        if (tokenHashedCredentials instanceof char[]) {
            tokenCredentials = String.valueOf((char[]) tokenHashedCredentials);
        } else {
            tokenCredentials = tokenHashedCredentials.toString();
        }
        tokenHashedCredentials = this.getPasswordEncoder().encode(tokenCredentials);
        Object accountCredentials = this.getCredentials(info);
        return this.equals(tokenHashedCredentials, accountCredentials);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
