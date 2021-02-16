package com.cory.web.controller;

import com.cory.context.GenericResult;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Cory on 2021/2/16.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CoryErrorController implements ErrorController {

    @Override
    @Deprecated
    public String getErrorPath() {
        return null;
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String errorHtml(HttpServletRequest request, Model model) {
        HttpStatus status = getStatus(request);

        model.addAttribute("errorType", status.value());
        model.addAttribute("errorPage", "true");
        return "index";
    }

    @RequestMapping
    @ResponseBody
    public GenericResult error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        return GenericResult.fail(status.value(), "Http Status Error");
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        }
        catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
