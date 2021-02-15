package com.cory.web.controller.base;

import com.cory.model.base.Datadict;
import com.cory.sevice.base.DatadictService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Controller
@RequestMapping("/base/datadict/")
public class DatadictController extends BaseAjaxController<Datadict> {

    @Autowired
    private DatadictService datadictService;

    public DatadictService getService() {
        return datadictService;
    }

    @RequestMapping("updateShowable")
    public boolean updateShowable(Integer id, boolean showable) {
        this.getService().updateShowable(id, showable);
        return true;
    }
}
