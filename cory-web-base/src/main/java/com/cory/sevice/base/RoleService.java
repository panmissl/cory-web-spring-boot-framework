package com.cory.sevice.base;

import com.cory.constant.base.CacheConstants;
import com.cory.dao.base.RoleDao;
import com.cory.model.base.Role;
import com.cory.service.BaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class RoleService extends BaseService<Role> {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private ResourceService resourceService;

    public RoleDao getDao() {
        return roleDao;
    }

    @CachePut(value = CacheConstants.Role)
    @Override
    public void add(Role model) {
        super.add(model);
    }

    @CacheEvict(value = {CacheConstants.User, CacheConstants.Role}, key = "#model.id", allEntries = true)
    @Override
    public void delete(Role model) {
        super.delete(model);
    }

    @CacheEvict(value = {CacheConstants.User, CacheConstants.Role}, key = "#id", allEntries = true)
    @Override
    public void delete(int id) {
        super.delete(id);
    }

    @CacheEvict(value = {CacheConstants.User, CacheConstants.Role}, key = "#model.id", allEntries = true)
    @Override
    public void update(Role model) {
        super.update(model);
    }

    @Cacheable(value = CacheConstants.Role, key = "'name-'.concat(#name)")
    public Role getByName(String name) {
        Role role = roleDao.getByName(name);
        assembleResource(role);
        return role;
    }

    @Cacheable(value = CacheConstants.Role, key = "#id")
    @Override
    public Role get(int id) {
        Role role = super.get(id);
        assembleResource(role);
        return role;
    }

    @Cacheable(value = CacheConstants.Role, key = "'userId-'.concat(#userId)")
    public List<Role> getByUser(Integer userId) {
        List<Role> roleList = roleDao.getByUser(userId);
        if (!CollectionUtils.isEmpty(roleList)) {
            for (Role role : roleList) {
                assembleResource(role);
            }
        }
        return roleList;
    }

    private void assembleResource(Role role) {
        if (null == role || null == role.getId() || role.getId() <= 0) {
            return;
        }
        role.setResources(resourceService.getByRole(role.getId()));
    }
}
