package com.cory.web.controller;

import com.cory.model.DataDict;
import com.cory.service.DatadictService;
import com.cory.util.systemconfigcache.SystemConfigCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基础数据请求Controller，比如数据字典，系统配置等
 * @author cory
 * @date 2022/2/7
 */
@Slf4j
@RestController
@RequestMapping("/ajax/basedata/")
public class BaseDataController extends BaseController {

    @Autowired
    private DatadictService datadictService;

    @GetMapping("datadict/list")
    public List<DataDict> dataDictList(Integer type) {
        return datadictService.getByType(type);
    }

    @GetMapping("systemconfig")
    public String systemConfig(String key) {
        return SystemConfigCacheUtil.getCache(key);
    }
}
