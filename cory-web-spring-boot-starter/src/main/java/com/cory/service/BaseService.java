package com.cory.service;

import com.cory.dao.BaseDao;
import com.cory.model.ActionLog;
import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import com.cory.web.util.ActionLogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Cory on 2017/5/13.
 * @author corypan
 * @date 2017/5/13
 */
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
        getDao().delete(model);
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
        getDao().updateModel(model);
        if (actionLogEnable() && !model.getClass().equals(ActionLog.class)) {
            ActionLogUtil.addActionLog(model.getClass().getName(), model.getId() + "", "修改数据");
        }
    }

    public T get(int id) {
        T t = getDao().get(id);
        fillOtherFields(t);
        return t;
    }

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param code
     * @return
     */
    public T getByCode(String code) {
        T t = getDao().getByCode(code);
        fillOtherFields(t);
        return t;
    }

    /**
     * 如果没有code字段，则此方法抛错。如果有则自动执行查询
     * @param codeList
     * @return
     */
    public List<T> getByCodeList(List<String> codeList) {
        List<T> list = getDao().getByCodeList(codeList);
        fillOtherFields(list);
        return list;
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

    protected List<T> fillOtherFields(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        return list.stream().map(m -> fillOtherFields(m)).collect(Collectors.toList());
    }

    protected T fillOtherFields(T model) {return model;}

    /**
     * 增删改时记录操作日志，默认记录，如果不需要可以复写返回false
     * @return
     */
    protected boolean actionLogEnable() {return true;}
}
