package com.cory.service;

import com.alibaba.fastjson.JSON;
import com.cory.constant.ErrorCode;
import com.cory.dao.BaseDao;
import com.cory.exception.CoryException;
import com.cory.model.ActionLog;
import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import com.cory.util.ModelUtil;
import com.cory.web.util.ActionLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Cory on 2017/5/13.
 * @author corypan
 * @date 2017/5/13
 */
@Slf4j
public abstract class BaseService<T extends BaseModel> {

    @Transactional(rollbackFor = Throwable.class)
    public void add(T model) {
        getDao().add(model);
        if (actionLogEnable() && !model.getClass().equals(ActionLog.class)) {
            ActionLogUtil.addActionLog(model.getClass().getName(), model.getId() + "", "添加数据");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void delete(T model) {
        getDao().deleteById(model.getId());
        if (actionLogEnable() && !model.getClass().equals(ActionLog.class)) {
            ActionLogUtil.addActionLog(model.getClass().getName(), model.getId() + "", "删除数据");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void delete(int id) {
        getDao().deleteById(id);
        if (actionLogEnable()) {
            ActionLogUtil.addActionLog(this.getClass().getName(), id + "", "根据ID删除数据");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(T model) {
        T db = getDao().get(model.getId());
        Field[] fields = db.getClass().getDeclaredFields();
        if (null != fields && fields.length > 0) {
            //将需要更新的字段设置
            for (Field field : fields) {
                com.cory.db.annotations.Field fieldAnno = field.getAnnotation(com.cory.db.annotations.Field.class);
                if (null == fieldAnno || !fieldAnno.updateable()) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    field.set(db, field.get(model));
                } catch (IllegalAccessException e) {
                    log.error("update fail, model: {}", JSON.toJSONString(model), e);
                    throw new CoryException(ErrorCode.SAVE_ERROR, "处理保存数据时失败");
                }
            }
        }

        ModelUtil.fillCreatorAndModifier(db);
        beforeUpdate(model, db);
        getDao().updateModel(db);
        if (actionLogEnable() && !model.getClass().equals(ActionLog.class)) {
            ActionLogUtil.addActionLog(model.getClass().getName(), model.getId() + "", "修改数据");
        }
    }

    /**
     * 在更新前的处理扩展点。已经将要更新的字段和数据库的合并了，如果有特殊处理的，可以在这里处理
     * @param model 前端提交上来的数据
     * @param dbModel 传入的更新字段和数据库的字段合并过的对象
     */
    protected void beforeUpdate(T model, T dbModel) {}

    public T get(int id) {
        return fillOtherFields(getDao().get(id));
    }

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param code
     * @return
     */
    public T getByCode(String code) {
        return fillOtherFields(getDao().getByCode(code));
    }

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param codeList
     * @return
     */
    public List<T> getByCodeList(List<String> codeList) {
        return fillOtherFields(getDao().getByCodeList(codeList));
    }

    /**
     *
     * @param pagination
     * @param model
     * @param sort default is ID DESC for empty sort
     * @return
     */
    public Pagination<T> list(Pagination<T> pagination, T model, String sort) {
        if (StringUtils.isBlank(sort)) {
            sort = "ID DESC";
        }
        int pageNo = pagination.getPageNo();
        int pageSize = pagination.getPageSize();
        pagination = getDao().pagination(model, (pagination.getPageNo() - 1) * pagination.getPageSize(), pagination.getPageSize(), sort);
        if (null != pagination) {
            pagination.setPageNo(pageNo);
            pagination.setPageSize(pageSize);
            fillOtherFields(pagination.getList());
        }
        return pagination;
    }

    public abstract <D extends BaseDao<T>> D getDao();

    /**
     * 一般情况下不用覆写此方法，除非有特殊需求
     * @param list
     * @return
     */
    protected List<T> fillOtherFields(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        return list.stream().map(m -> fillOtherFields(m)).collect(Collectors.toList());
    }

    /**
     * @param model 注意判断null
     * @return
     */
    protected T fillOtherFields(T model) {return model;}

    /**
     * 增删改时记录操作日志，默认不记录，如果需要可以复写返回true。其它方法可以自己写操作日志
     * @return
     */
    protected boolean actionLogEnable() {return false;}
}
