package com.cory.scheduler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by Cory on 2021/2/9.
 */
@ConfigurationProperties(prefix = Constant.PREFIX)
@Data
public class CorySchedulerProperties {

    /** 是否启用 */
    private boolean enable = false;

    /**
     * job配置：Job(Job类名):cronExpress。如：SampleJob:0 0 2 * * ?
     */
    private List<String> jobConfigs;

}
