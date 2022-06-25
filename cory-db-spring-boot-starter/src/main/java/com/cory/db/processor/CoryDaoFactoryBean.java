package com.cory.db.processor;

import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.CoryDbProxy;
import com.cory.db.datapermission.DataPermission;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Cory on 2021/2/9.
 */
public class CoryDaoFactoryBean<T> implements FactoryBean<T> {

    private ApplicationContext ctx;
    private Class<T> cls;

    @Nullable
    @Override
    public T getObject() throws Exception {
        CoryDb coryDb = ctx.getBean(CoryDb.class);
        List<DataPermission> dataPermissionList = parseDataPermission();
        CoryDbProperties coryDbProperties = ctx.getBean(CoryDbProperties.class);
        return CoryDbProxy.newMapperProxy(cls, coryDb, coryDbProperties.isLogEnable(), dataPermissionList);
    }

    private List<DataPermission> parseDataPermission() {
        Map<String, DataPermission> dataPermissionMap = ctx.getBeansOfType(DataPermission.class);
        if (MapUtils.isEmpty(dataPermissionMap)) {
            return Lists.newArrayList();
        }
        return dataPermissionMap.values().stream().collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Class<?> getObjectType() {
        return cls;
    }

    public void setCtx(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public void setCls(Class<T> cls) {
        this.cls = cls;
    }
}
