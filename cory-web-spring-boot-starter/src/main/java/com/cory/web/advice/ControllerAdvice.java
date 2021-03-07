package com.cory.web.advice;

import com.cory.db.annotations.Field;
import com.cory.db.enums.CoryDbType;
import com.cory.model.BaseModel;
import com.cory.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Cory on 2021/3/6.
 */
@Configuration
@Aspect
public class ControllerAdvice {

    @Pointcut("execution(public * com.cory..*Controller.*(..))")
    public void point(){}

    @Before("point()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (null != args && args.length > 0) {
            for (Object arg : args) {
                parseDateParams(arg);
            }
        }
    }

    private void parseDateParams(Object arg) {
        if (null == arg) {
            return;
        }
        if (!(arg instanceof BaseModel)) {
            return;
        }
        BaseModel model = (BaseModel) arg;

        java.lang.reflect.Field[] fields = arg.getClass().getDeclaredFields();
        if (null != fields || fields.length > 0) {
            for (java.lang.reflect.Field javaField : fields) {
                Field field = javaField.getAnnotation(Field.class);
                if (null == field) {
                    continue;
                }
                //对date型参数，增加Start和End的解析，并放到Filter参数里
                if (CoryDbType.DATE.equals(field.type()) || CoryDbType.DATETIME.equals(field.type())) {
                    String startParam = javaField.getName() + "Start";
                    String endParam = javaField.getName() + "End";
                    parseDateParams(model, startParam, CoryDbType.DATE.equals(field.type()), false);
                    parseDateParams(model, endParam, CoryDbType.DATE.equals(field.type()), true);
                }
            }
        }
    }

    private void parseDateParams(BaseModel model, String paramName, boolean isDate, boolean isEnd) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String date = request.getParameter(paramName);
        if (StringUtils.isBlank(date)) {
            return;
        }
        try {
            //DATE型要处理成yyyy-MM-dd，不要解析成Date型即可
            //END因为是exclusion，所以要加1：对于时间加1秒，对于日期加1天
            if (isEnd) {
                Date d = DateUtils.parseDate(date);
                if (isDate) {
                    d = DateUtils.addDays(d, 1);
                } else {
                    d = DateUtils.addSeconds(d, 1);
                }
                model.getFilterFieldMap().put(paramName, isDate ? DateUtils.formatShort(d) : d);
            } else {
                model.getFilterFieldMap().put(paramName, isDate ? date : DateUtils.parseDate(date));
            }
        } catch (ParseException e) {
        }
    }
}
