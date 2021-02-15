package com.cory.web.controller;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.GenericResult;
import com.cory.exception.CoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Cory on 2017/5/14.
 */
@RestController
@Slf4j
public abstract class BaseController {

    @ExceptionHandler
    public GenericResult resolveException(Throwable t, HttpServletRequest request) {
        log.error("error occurred.", t);

        request.setAttribute(Constants.EXCEPTION_ATTR, true);

        CoryException ex;
        if (t instanceof CoryException) {
            ex = (CoryException) t;
        } else {
            ex = new CoryException(ErrorCode.GENERIC_ERROR, t.getMessage());
        }
        return GenericResult.fail(ex);
    }
}
