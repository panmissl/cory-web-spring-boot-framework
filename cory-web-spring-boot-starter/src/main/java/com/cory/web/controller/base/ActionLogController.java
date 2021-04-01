package com.cory.web.controller.base;

import com.cory.model.ActionLog;
import com.cory.service.ActionLogService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/actionlog/")
public class ActionLogController extends BaseAjaxController<ActionLog> {

    @Autowired
    private ActionLogService actionLogService;

    public ActionLogService getService() {
        return actionLogService;
    }

    @Override
    public boolean delete(@PathVariable int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int save(ActionLog entity) {
        throw new UnsupportedOperationException();
    }
}