package com.cory.dao;

import com.cory.db.annotations.Dao;
import com.cory.model.SystemConfig;

/**
 * Created by Cory on 2017/5/10.
 */
@Dao(model = SystemConfig.class)
public interface SystemConfigDao extends BaseDao<SystemConfig> {
}
