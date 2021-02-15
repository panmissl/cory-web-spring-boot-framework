package com.cory.web.controller;

import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import com.cory.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Cory on 2017/5/14.
 */
@Slf4j
@RequestMapping("/ajax")
public abstract class BaseAjaxController<T extends BaseModel> extends BaseController {

    @GetMapping("listData")
    public Pagination<T> list(Pagination<T> pagination, T t, String sort) {
        return getService().list(pagination, t, sort);
    }

    @GetMapping("detailData/{id}")
    public T detailData(@PathVariable int id) {
        return getService().get(id);
    }

    @PostMapping(value="delete/{id}")
    public boolean delete(@PathVariable int id) {
        getService().delete(id);
        return true;
    }

    @PostMapping(value="save")
    public int save(T entity) {
        if (null != entity.getId() && entity.getId() > 0) {
            getService().update(entity);
        } else {
            getService().add(entity);
        }
        return entity.getId();
    }

    public abstract <S extends BaseService<T>> S getService();
}
