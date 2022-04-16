package com.cory.service;

import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.context.CurrentUser;
import com.cory.dao.DatadictDao;
import com.cory.exception.CoryException;
import com.cory.model.DataDict;
import com.cory.page.Pagination;
import com.cory.util.datadictcache.DataDictCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Slf4j
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DatadictService extends BaseService<DataDict> {

    public static final String TYPE_DESC_NAME = "typeDesc";

    @Autowired
    private DatadictDao datadictDao;

    //此类不用缓存，因为会加载到Util里去，目前Util是单机版，后期考虑Util的分布式版本，否则更新缓存只能靠一直刷新轮询到或重启

    @Override
    public DatadictDao getDao() {
        return datadictDao;
    }

    @Override
    public Pagination<DataDict> list(int pageNo, int pageSize, DataDict model, String sort) {
        return super.list(pageNo, pageSize, model, "SN");
    }

    @Override
    public void delete(DataDict model) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Override
    public void add(DataDict model) {
        if (null != datadictDao.getByValue(model.getType(), model.getValue())) {
            throw new CoryException(ErrorCode.SAVE_ERROR, "code为" + model.getValue() + "的记录已经存在，请重新输入.");
        }
        super.add(model);
    }

    @Override
    public void update(DataDict model) {
        DataDict db = datadictDao.get(model.getId());
        if (!StringUtils.equals(model.getValue(), db.getValue())) {
            if (null != datadictDao.getByValue(model.getType(), model.getValue())) {
                throw new CoryException(ErrorCode.SAVE_ERROR, "值" + model.getValue() + "已经存在.");
            }
        }
        super.update(model);
        DataDictCacheUtil.refresh(convert2CacheDTO(model));
    }

    public void updateShowable(Integer id, boolean showable) {
        this.getDao().updateShowable(id, showable, CurrentUser.get().getId());
        DataDictCacheUtil.refresh(convert2CacheDTO(get(id)));
    }

    public DataDict getByValue(String type, String value) {
        DataDict dd = this.getDao().getByValue(type, value);
        return fillOtherFields(dd);
    }

    public List<DataDict> getByType(String type) {
        List<DataDict> list = this.getDao().getByType(type, "SN");
        fillOtherFields(list);
        return list;
    }

    @Override
    protected DataDict fillOtherFields(DataDict model) {
        if (null == model) {
            return null;
        }
        model.getRenderFieldMap().put(TYPE_DESC_NAME, parseTypeDesc(model));
        return model;
    }

    private String parseTypeDesc(DataDict model) {
        if (Constants.DATA_DICT_ROOT_VALUE.equals(model.getValue()) || Constants.DATA_DICT_ROOT_VALUE.equals(model.getType())) {
            return "ROOT(根类型)";
        }
        DataDict type = this.getDao().getByValue(Constants.DATA_DICT_ROOT_VALUE, model.getType());
        if (null == type) {
            return "无";
        }
        return type.getValue() + "(" + type.getDescription() + ")";
    }

    public void refreshCache() {
        log.info("load data dict to cache...");
        Pagination<DataDict> p = this.list(1, Integer.MAX_VALUE, null, null);
        List<DataDict> list = p.getList();
        if (!CollectionUtils.isEmpty(list)) {
            for (DataDict dd : list) {
                DataDictCacheUtil.refresh(convert2CacheDTO(dd));
            }
        }
        log.info("load data dict to cache finish, count: {}", p.getTotalCount());
    }

    private DataDictCacheUtil.DataDict convert2CacheDTO(DataDict dd) {
        if (null == dd) {
            return null;
        }
        return DataDictCacheUtil.DataDict.builder()
                .id(dd.getId())
                .type(dd.getType())
                .typeDesc(dd.getRenderFieldMap().get(TYPE_DESC_NAME))
                .value(dd.getValue())
                .sn(dd.getSn())
                .description(dd.getDescription())
                .showable(dd.getShowable())
                .build();
    }
}
