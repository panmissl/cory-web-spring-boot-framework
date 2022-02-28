package com.cory.web.controller.base;

import com.cory.model.AccessCount;
import com.cory.service.AccessCountService;
import com.cory.vo.AccessCountStatVO;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/accesscount/")
public class AccessCountController extends BaseAjaxController<AccessCount> {

    @Autowired
    private AccessCountService accessCountService;

    public AccessCountService getService() {
        return accessCountService;
    }

    @GetMapping("stat")
    public AccessCountStatVO stat() {
        return accessCountService.stat();
    }
}
