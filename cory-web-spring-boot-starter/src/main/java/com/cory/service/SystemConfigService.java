package com.cory.service;

import com.cory.dao.SystemConfigDao;
import com.cory.model.SystemConfig;
import com.cory.page.Pagination;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    //此类不用缓存，因为会加载到Util里去，目前Util是单机版，后期考虑Util的分布式版本，否则更新缓存只能靠一直刷新轮询到或重启

    @Override
    public void add(SystemConfig model) {
        super.add(model);
    }

    @Override
    public void delete(SystemConfig model) {
        super.delete(model);
    }

    @Override
    public void delete(int id) {
        super.delete(id);
    }

    @Override
    public void update(SystemConfig model) {
        super.update(model);
    }

    @Override
    public SystemConfig get(int id) {
        return super.get(id);
    }

    public void refreshCache() {
        log.info("加载系统参数配置...");
        Pagination<SystemConfig> p = this.list(1, Integer.MAX_VALUE, null, null);
        List<SystemConfig> list = p.getList();
        if (!CollectionUtils.isEmpty(list)) {
            for (SystemConfig sc : list) {
                SystemConfigCacheUtil.refresh(sc.getCode(), sc.getVal());
            }
        }
        log.info("加载系统参数配置完成(共{}条).", p.getTotalCount());
    }

    @Override
    public SystemConfigDao getDao() {
        return systemConfigDao;
    }
}
