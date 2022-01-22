package com.cory.db.processor;

import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.CoryDbProxy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

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
        CoryDbProperties coryDbProperties = ctx.getBean(CoryDbProperties.class);
        return CoryDbProxy.newMapperProxy(cls, coryDb, coryDbProperties.isLogEnable());
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
