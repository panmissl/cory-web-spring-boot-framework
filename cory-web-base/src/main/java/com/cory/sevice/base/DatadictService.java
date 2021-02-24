package com.cory.sevice.base;

import com.cory.constant.ErrorCode;
import com.cory.constant.CacheConstants;
import com.cory.context.CurrentUser;
import com.cory.dao.base.DatadictDao;
import com.cory.exception.CoryException;
import com.cory.model.base.DataDict;
import com.cory.page.Pagination;
import com.cory.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * generated by CodeGenerator on 2017/5/10.
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class DatadictService extends BaseService<DataDict> {

    @Autowired
    private DatadictDao datadictDao;

    public DatadictDao getDao() {
        return datadictDao;
    }

    @Override
    public Pagination<DataDict> list(Pagination<DataDict> pagination, DataDict model, String sort) {
        return super.list(pagination, model, "SN DESC");
    }

    @Override
    public void delete(DataDict model) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Cacheable(value = CacheConstants.Datadict, key = "#id")
    @Override
    public DataDict get(int id) {
        return super.get(id);
    }

    @CacheEvict(value = CacheConstants.Datadict, allEntries = true)
    @Override
    public void add(DataDict model) {
        if (null != this.getByValue(model.getValue())) {
            throw new CoryException(ErrorCode.SAVE_ERROR, "code为" + model.getValue() + "的记录已经存在，请重新输入.");
        }
        super.add(model);
    }

    @CacheEvict(value = CacheConstants.Datadict, key = "#model.id", allEntries = true)
    @Override
    public void update(DataDict model) {
        if (!StringUtils.equals(model.getValue(), this.get(model.getId()).getValue())) {
            throw new CoryException(ErrorCode.SAVE_ERROR, "code不能修改.");
        }
        super.update(model);
    }

    @CacheEvict(value = CacheConstants.Datadict, key = "#id", allEntries = true)
    public void updateShowable(Integer id, boolean showable) {
        this.getDao().updateShowable(id, showable, CurrentUser.get().getId());
    }

    @Cacheable(value = CacheConstants.Datadict, key = "'value-'.concat(#value)")
    public DataDict getByValue(String value) {
        DataDict dd = this.getDao().getByValue(value);
        fillOtherFields(dd);
        return dd;
    }

    @Cacheable(value = CacheConstants.Datadict, key = "'all-types'")
    public List<DataDict> getAllTypes() {
        List<DataDict> list = this.getDao().getAllTypes("SN DESC");
        fillOtherFields(list);
        return list;
    }

    @Cacheable(value = CacheConstants.Datadict, key = "'type-'.concat(#type)")
    public List<DataDict> getByType(Integer type) {
        List<DataDict> list = this.getDao().getByType(type, "SN DESC");
        fillOtherFields(list);
        return list;
    }

    @Override
    protected void fillOtherFields(DataDict model) {
        if (null == model) {
            return;
        }
        String typeDesc = "ROOT(根类型)";
        if (null != model.getType() && model.getType() > 0) {
            DataDict type = this.getDao().get(model.getType());
            typeDesc = type.getValue() + "(" + type.getDescription() + ")";
        }
        model.getRenderFields().put("typeDesc", typeDesc);
    }
}
