package com.cory.db.processor;

import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.CoryDbProxy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

/**
 * Created by Cory on 2021/2/9.
 */
public class CoryDaoFactoryBean<T> implements FactoryBean<T> {

    private Class<T> cls;
    private CoryDb coryDb;
    private boolean logEnable;

    @Nullable
    @Override
    public T getObject() throws Exception {
        return CoryDbProxy.newMapperProxy(cls, coryDb, logEnable);
    }

    @Nullable
    @Override
    public Class<?> getObjectType() {
        return cls;
    }
}
