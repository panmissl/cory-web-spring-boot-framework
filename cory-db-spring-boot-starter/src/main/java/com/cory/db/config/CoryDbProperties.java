package com.cory.db.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Cory on 2021/2/9.
 */
@ConfigurationProperties(prefix = Constant.PREFIX)
@Data
public class CoryDbProperties {

    /** 是否启动数据库 */
    private boolean enable = true;

    /** DAO包，可以指定多个 */
    private String[] daoPackages = new String[] {"com.cory.dao"};

    /** Model包，可以指定多个 */
    private String[] modelPackages = new String[] {"com.cory.model"};

    /** 是否打印log */
    private boolean logEnable = true;

}
