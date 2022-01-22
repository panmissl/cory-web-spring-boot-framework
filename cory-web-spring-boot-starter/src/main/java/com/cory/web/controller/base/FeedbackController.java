package com.cory.web.controller.base;

import com.cory.model.Feedback;
import com.cory.service.FeedbackService;
import com.cory.web.controller.BaseAjaxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@RestController
@RequestMapping("/ajax/base/feedback/")
public class FeedbackController extends BaseAjaxController<Feedback> {

    @Autowired
    private FeedbackService feedbackService;

    public FeedbackService getService() {
        return feedbackService;
    }
}
