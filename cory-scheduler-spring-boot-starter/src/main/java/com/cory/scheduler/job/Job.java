package com.cory.scheduler.job;

/**
 * 单台机器任务继承SingleIpJob，如需要广播任务直接实现Job接口
 *
 * Created by Cory on 2017/5/29.
 * @author corypan
 */
public interface Job {

    void run();
}
