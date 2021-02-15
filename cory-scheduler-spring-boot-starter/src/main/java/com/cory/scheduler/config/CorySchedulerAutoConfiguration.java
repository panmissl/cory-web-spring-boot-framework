package com.cory.scheduler.config;

import com.cory.scheduler.job.Job;
import com.cory.util.AssertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 使用前，在application.properties文件里配置数据库信息：spring.datasource.username、spring.datasource.password、spring.datasource.name
 * <br />
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CorySchedulerProperties.class)
public class CorySchedulerAutoConfiguration {

    @Autowired
    private CorySchedulerProperties corySchedulerProperties;
    @Autowired
    private List<Job> jobList;

    @Bean
    @ConditionalOnProperty(prefix = Constant.PREFIX, name = "enable", havingValue = "true")
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        if (CollectionUtils.isNotEmpty(jobList)) {
            Map<String, String> jobConfig = jobConfig();
            List<CronTrigger> triggerList = jobList.stream().map(job -> buildTrigger(job, jobConfig)).collect(Collectors.toList());
            bean.setTriggers(triggerList.toArray(new CronTrigger[triggerList.size()]));
        }
        return bean;
    }

    private CronTrigger buildTrigger(Job job, Map<String, String> jobConfig) {
        MethodInvokingJobDetailFactoryBean detail = new MethodInvokingJobDetailFactoryBean();
        detail.setConcurrent(false);
        detail.setTargetObject(job);
        detail.setTargetMethod("run");

        String cronExpression = jobConfig.get(job.getClass().getSimpleName());
        AssertUtils.hasText(cronExpression, "job(" + job.getClass().getName() + ")的执行时间没有配置，请在application.properties里配置。");

        CronTriggerFactoryBean bean = new CronTriggerFactoryBean();
        bean.setJobDetail(detail.getObject());
        bean.setCronExpression(cronExpression);

        return bean.getObject();
    }

    /**
     * @return key: Job, value: cronExpression
     */
    private Map<String, String> jobConfig() {
        if (CollectionUtils.isEmpty(corySchedulerProperties.getJobConfigs())) {
            return new HashMap<>();
        }
        return corySchedulerProperties.getJobConfigs().stream().collect(Collectors.toMap(
                config -> config.split(":")[0],
                config -> config.split(":")[1],
                (v1, v2) -> v2
        ));
    }

}
