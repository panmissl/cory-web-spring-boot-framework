package com.cory.web.controller.base;

import com.cory.model.ClusterJob;
import com.cory.service.ClusterJobService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/clusterjob/")
public class ClusterJobController extends BaseAjaxController<ClusterJob> {

    @Autowired
    private ClusterJobService clusterJobService;

    public ClusterJobService getService() {
        return clusterJobService;
    }
}
