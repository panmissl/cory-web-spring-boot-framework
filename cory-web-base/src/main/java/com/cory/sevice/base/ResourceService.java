package com.cory.sevice.base;

import com.cory.constant.base.CacheConstants;
import com.cory.dao.base.ResourceDao;
import com.cory.model.base.Resource;
import com.cory.service.BaseService;
import com.cory.sevice.base.resource.ResourceScanner;
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
public class ResourceService extends BaseService<Resource> {

    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private ResourceScanner resourceScanner;

    @CachePut(value = CacheConstants.Resource)
    @Override
    public void add(Resource model) {
        super.add(model);
    }

    @CacheEvict(value = {CacheConstants.Role, CacheConstants.Resource}, key = "#model.id", allEntries = true)
    @Override
    public void delete(Resource model) {
        super.delete(model);
    }

    @CacheEvict(value = {CacheConstants.Role, CacheConstants.Resource}, key = "#id", allEntries = true)
    @Override
    public void delete(int id) {
        super.delete(id);
    }

    @CacheEvict(value = {CacheConstants.Role, CacheConstants.Resource}, key = "#model.id", allEntries = true)
    @Override
    public void update(Resource model) {
        super.update(model);
    }

    @Cacheable(value = CacheConstants.Resource, key = "#id")
    @Override
    public Resource get(int id) {
        return super.get(id);
    }

    public ResourceDao getDao() {
        return resourceDao;
    }

    @Cacheable(value = CacheConstants.Resource, key = "'roleId-'.concat(#roleId)")
    public List<Resource> getByRole(Integer roleId) {
        return resourceDao.getByRole(roleId);
    }

    public void scanResourceAndLoadToDb() {
        resourceScanner.scanAndLoadToDb();
    }

    @Cacheable(value = CacheConstants.Resource, key = "'value-'.concat(#value)")
    public Resource getByValue(String value) {
        return resourceDao.getByValue(value);
    }
}
