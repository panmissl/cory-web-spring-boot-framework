package com.cory.web.controller.base;

import com.cory.model.SystemConfig;
import com.cory.service.SystemConfigService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("refreshCache")
    public boolean refreshCache() {
        systemConfigService.refreshCache();
        return true;
    }

    public SystemConfigService getService() {
        return systemConfigService;
    }
}
