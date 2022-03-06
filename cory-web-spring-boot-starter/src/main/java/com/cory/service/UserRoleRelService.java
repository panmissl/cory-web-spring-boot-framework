package com.cory.service;

import com.cory.constant.CacheConstants;
import com.cory.dao.UserRoleRelDao;
import com.cory.model.UserRoleRel;
import com.cory.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class UserRoleRelService extends BaseService<UserRoleRel> {

    @Autowired
    private UserRoleRelDao userRoleRelDao;

    @Override
    public UserRoleRelDao getDao() {
        return userRoleRelDao;
    }

    @CacheEvict(value = {CacheConstants.User, CacheConstants.Role}, allEntries = true)
    public void assign(UserRoleRel userRoleRel) {
        userRoleRelDao.deleteByUser(userRoleRel.getUserId());
        userRoleRelDao.add(userRoleRel);
    }
}
