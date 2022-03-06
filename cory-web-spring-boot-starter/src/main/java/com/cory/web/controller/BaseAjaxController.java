package com.cory.web.controller;

import com.cory.model.BaseModel;
import com.cory.page.Pagination;
import com.cory.service.BaseService;
import com.cory.util.ModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Cory on 2017/5/14.
 */
@Slf4j
@RestController
//@RequestMapping("/ajax/")
public abstract class BaseAjaxController<T extends BaseModel> extends BaseController {

    @GetMapping("listData")
    public Pagination<T> list(@RequestParam(required = false, defaultValue = "1") int pageNo,
                              @RequestParam(required = false, defaultValue = "20") int pageSize,
                              T t,
                              String sort) {
        return getService().list(pageNo, pageSize, t, sort);
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
    public int save(@Validated T entity) {
        ModelUtil.fillCreatorAndModifier(entity);
        if (null != entity.getId() && entity.getId() > 0) {
            getService().update(entity);
        } else {
            getService().add(entity);
        }
        return entity.getId();
    }

    public abstract <S extends BaseService<T>> S getService();
}
