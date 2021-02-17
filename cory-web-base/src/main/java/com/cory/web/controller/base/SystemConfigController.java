package com.cory.web.controller.base;

import com.cory.model.base.SystemConfig;
import com.cory.sevice.base.SystemConfigService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Cory on 2017/5/10.
 */
@Controller
@RequestMapping("/ajax/base/systemconfig/")
public class SystemConfigController extends BaseAjaxController<SystemConfig> {

    @Autowired
    private SystemConfigService systemConfigService;

    @RequestMapping("refreshCache")
    @ResponseBody
    public boolean refreshCache() {
        systemConfigService.refreshCache();
        return true;
    }

    public SystemConfigService getService() {
        return systemConfigService;
    }
}
