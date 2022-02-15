package com.cory.scheduler.job;

import com.cory.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * 单台机器任务，如需要广播任务直接实现Job接口
 * Created by Cory on 2017/5/29.
 */
@Slf4j
public abstract class SingleIpJob implements Job {

    private boolean running = false;

    @Value("${server.port}")
    private Integer port;

    @Override
    public final void run() {
        if (!shouldRun()) {
            return;
        }

        synchronized (this) {
            if (running) {
                log.info("previous job is running. I[{}] will exit.", Thread.currentThread());
                return;
            }
            running = true;
        }

        try {
            log.info("run single ip job.");
            doRun();
        } catch (Throwable t) {
            log.error("error happened when running job.", t);
        } finally {
            running = false;
        }
    }

    protected boolean shouldRun() {
        String ip = getRunIpAndPort();
        String host = IpUtil.getHostIp();
        host += ":" + port;
        if (StringUtils.isEmpty(ip) || !host.equalsIgnoreCase(ip.trim())) {
            log.info("I am not the server wanted, skip run. Run ip & port is {}, my ip & port is {}", ip, host);
            return false;
        } else {
            return true;
        }
    }

    /** 要执行的机器+端口。如：192.168.1.1:8080 */
    protected abstract String getRunIpAndPort();

    /** 具体任务执行入口 */
    protected abstract void doRun();
}
