package com.cory.scheduler.job;

import com.cory.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Cory on 2017/5/29.
 */
@Slf4j
public abstract class SingleIpJob implements Job {

    private boolean running = false;

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
        String ip = getRunIp();
        String host = IpUtil.getHostIp();
        if (StringUtils.isEmpty(ip) || !host.equalsIgnoreCase(ip.trim())) {
            log.info("I am not the server wanted, skip run. Run ip is {}, my ip is {}", ip, host);
            return false;
        } else {
            return true;
        }
    }

    protected abstract String getRunIp();

    protected abstract void doRun();
}
