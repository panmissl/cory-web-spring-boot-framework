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

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by Cory on 2017/5/10.
 */
@Slf4j
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class SystemConfigService extends BaseService<SystemConfig> {

    private static final String CLUSTER_JOB_CODE = "refresh_system_config_cache";

    @Autowired
    private SystemConfigDao systemConfigDao;
    @Autowired
    private ClusterJobService clusterJobService;

    @PostConstruct
    public void init() {
        clusterJobService.registerJobHandler(CLUSTER_JOB_CODE, param -> refreshCache());
    }

    //此类不用缓存，因为会加载到Util里去

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
        log.info("load system config to cache...");
        Pagination<SystemConfig> p = this.list(1, Integer.MAX_VALUE, null, null);
        List<SystemConfig> list = p.getList();
        if (!CollectionUtils.isEmpty(list)) {
            for (SystemConfig sc : list) {
                SystemConfigCacheUtil.refresh(sc.getCode(), sc.getVal());
            }
        }
        log.info("load system config to cache finish, count: {}", p.getTotalCount());
    }

    @Override
    public SystemConfigDao getDao() {
        return systemConfigDao;
    }

    public void addRefreshJob() {
        clusterJobService.addJob(CLUSTER_JOB_CODE, "刷新系统配置缓存", null);
    }
}
