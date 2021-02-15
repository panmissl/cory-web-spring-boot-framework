package com.cory.scheduler.config;

import com.cory.scheduler.job.Job;
import com.cory.util.AssertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronTrigger;
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
 * Created by Cory on 2021/2/9.
 */
@Configuration
@ConditionalOnProperty(prefix = Constant.PREFIX, name = "enable", havingValue = "true")
@EnableConfigurationProperties(CorySchedulerProperties.class)
public class CorySchedulerAutoConfiguration {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(List<Job> jobList, CorySchedulerProperties corySchedulerProperties) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        if (CollectionUtils.isNotEmpty(jobList)) {
            Map<String, String> jobConfig = jobConfig(corySchedulerProperties);
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
    private Map<String, String> jobConfig(CorySchedulerProperties corySchedulerProperties) {
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
