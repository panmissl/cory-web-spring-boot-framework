package com.cory.web.util;

import com.cory.context.CurrentUser;
import com.cory.model.ActionLog;
import com.cory.service.ActionLogService;
import com.cory.util.ModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Cory on 2017/5/28.
 */
@Slf4j
@Component
public class ActionLogUtil implements ApplicationContextAware {

    private static final String DEFAULT_USER = "NULL";
    private static ApplicationContext ctx;

    /**
     * 操作人用当前登录人，没有登录人写NULL
     * <br />
     * 操作时间用当前时间
     * @param objectType
     * @param objectId
     * @param log
     */
    public static void addActionLog(String objectType, String objectId, String log) {
        addActionLog(objectType, objectId, log, defaultUser(), new Date());
    }

    /**
     * 操作时间用当前时间
     * @param objectType
     * @param objectId
     * @param log
     * @param operator
     */
    public static void addActionLog(String objectType, String objectId, String log, String operator) {
        addActionLog(objectType, objectId, log, operator, new Date());
    }

    /**
     * 操作人用当前登录人，没有登录人写NULL
     * @param objectType
     * @param objectId
     * @param log
     * @param operateTime
     */
    public static void addActionLog(String objectType, String objectId, String log, Date operateTime) {
        addActionLog(objectType, objectId, log, defaultUser(), operateTime);
    }

    /**
     * 指定操作人和操作时间记录日志
     * @param objectType
     * @param objectId
     * @param logValue
     * @param operator
     * @param operateTime
     */
    public static void addActionLog(String objectType, String objectId, String logValue, String operator, Date operateTime) {
        if (null == ctx) {
            log.error("application context is null");
            return;
        }
        ActionLogService service = ctx.getBean(ActionLogService.class);
        if (null == service) {
            log.error("action log service is null");
            return;
        }
        ActionLog al = ActionLog.builder()
                .objectId(objectId)
                .objectType(objectType)
                .operator(StringUtils.isBlank(operator) ? DEFAULT_USER : operator)
                .operateTime(null == operateTime ? new Date() : operateTime)
                .log(logValue)
                .build();
        ModelUtil.fillCreatorAndModifier(al);
        service.add(al);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    private static String defaultUser() {
        String user = CurrentUser.get().getPrincipal();
        if (StringUtils.isBlank(user)) {
            user = DEFAULT_USER;
        }
        return user;
    }
}
