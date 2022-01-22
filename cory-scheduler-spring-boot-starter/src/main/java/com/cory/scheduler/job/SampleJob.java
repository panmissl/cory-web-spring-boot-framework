package com.cory.scheduler.job;

import org.springframework.stereotype.Service;

/**
 * Created by Cory on 2017/5/29.
 */
@Service
public class SampleJob extends SingleIpJob {

    protected String getRunIp() {
        //return SystemConfigCacheUtil.getCache(SystemConfigCacheKey.QUARTZ_IP);
        return "192.168.1.1";
    }

    protected void doRun() {
        //do run here
    }
}
