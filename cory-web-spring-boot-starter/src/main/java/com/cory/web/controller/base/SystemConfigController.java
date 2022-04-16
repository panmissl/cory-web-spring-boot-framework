package com.cory.web.controller.base;

import com.cory.model.SystemConfig;
import com.cory.service.SystemConfigService;
import com.cory.util.IpUtil;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Cory on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/systemconfig/")
public class SystemConfigController extends BaseAjaxController<SystemConfig> {

    @Autowired
    private SystemConfigService systemConfigService;
    @Value("${server.port}")
    private Integer port;

    @RequestMapping("refreshCache")
    public boolean refreshCache() {
        systemConfigService.addRefreshJob();
        return true;
    }

    @GetMapping("ip_port")
    public String ipAndPort() {
        return IpUtil.getHostIp() + ":" + port;
    }

    public SystemConfigService getService() {
        return systemConfigService;
    }
}
