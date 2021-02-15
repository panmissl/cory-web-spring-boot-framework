package com.cory.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by Cory on 2021/2/9.
 */
@ConfigurationProperties(prefix = Constant.PREFIX)
@Data
public class CoryCacheProperties {

    /** 是否打印log */
    private boolean logEnable = true;

    /** 类型：simple/redis/etcd */
    private String type;

    /** etcd 服务器列表 */
    private List<String> etcdServers;

}
