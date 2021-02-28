package com.cory.service;

import com.cory.dao.BaseDao;
import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by Cory on 2017/5/13.
 */
public abstract class BaseService<T extends BaseModel> {

    public void add(T model) {
        getDao().add(model);
    }

    public void delete(T model) {
        getDao().delete(model);
    }

    public void delete(int id) {
        getDao().deleteById(id);
    }

    public void update(T model) {
        getDao().updateModel(model);
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

    protected void fillOtherFields(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(model -> fillOtherFields(model));
    }

    protected void fillOtherFields(T model) {}
}
