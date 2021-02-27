package com.cory.service;

import com.cory.constant.CacheConstants;
import com.cory.dao.SystemConfigDao;
import com.cory.model.SystemConfig;
import com.cory.page.Pagination;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import lombok.extern.slf4j.Slf4j;
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
 * Created by Cory on 2017/5/10.
 */
@Slf4j
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class SystemConfigService extends BaseService<SystemConfig> {

    @Autowired
    private SystemConfigDao systemConfigDao;

    @CachePut(value = CacheConstants.SystemConfig)
    @Override
    public void add(SystemConfig model) {
        super.add(model);
    }

    @CacheEvict(value = CacheConstants.SystemConfig, key = "#model.id")
    @Override
    public void delete(SystemConfig model) {
        super.delete(model);
    }

    @CacheEvict(value = CacheConstants.SystemConfig, key = "#id")
    @Override
    public void delete(int id) {
        super.delete(id);
    }

    @CacheEvict(value = CacheConstants.SystemConfig, key = "#model.id")
    @Override
    public void update(SystemConfig model) {
        super.update(model);
    }

    @Cacheable(value = CacheConstants.SystemConfig, key = "#id")
    @Override
    public SystemConfig get(int id) {
        return super.get(id);
    }

    public void refreshCache() {
        log.info("加载系统参数配置...");
        Pagination<SystemConfig> p = new Pagination<>();
        p.setPageNo(1);
        p.setPageSize(Integer.MAX_VALUE);
        p = this.list(p, null, null);
        List<SystemConfig> list = p.getList();
        if (!CollectionUtils.isEmpty(list)) {
            for (SystemConfig sc : list) {
                SystemConfigCacheUtil.refresh(sc.getCode(), sc.getVal());
            }
        }
        log.info("加载系统参数配置完成(共{}条).", p.getTotalCount());
    }

    public SystemConfigDao getDao() {
        return systemConfigDao;
    }
}
