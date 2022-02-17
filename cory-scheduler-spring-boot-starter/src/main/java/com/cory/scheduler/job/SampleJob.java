package com.cory.scheduler.job;

/**
 * Created by Cory on 2017/5/29.
 */
//@Service
public class SampleJob extends SingleIpJob {

    @Override
    protected String getRunIpAndPort() {
        //return SystemConfigCacheUtil.getCache(SystemConfigCacheKey.QUARTZ_IP);
        return "192.168.1.1:8080";
    }

    @Override
    protected void doRun() {
        //do run here
    }
}
