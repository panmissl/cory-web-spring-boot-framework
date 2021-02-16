package com.cory.sevice.base;

import com.cory.constant.ErrorCode;
import com.cory.constant.base.CacheConstants;
import com.cory.context.CurrentUser;
import com.cory.dao.base.DatadictDao;
import com.cory.exception.CoryException;
import com.cory.model.base.Datadict;
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
public class DatadictService extends BaseService<Datadict> {

    @Autowired
    private DatadictDao datadictDao;

    public DatadictDao getDao() {
        return datadictDao;
    }

    @Override
    public Pagination<Datadict> list(Pagination<Datadict> pagination, Datadict model, String sort) {
        return super.list(pagination, model, "SN DESC");
    }

    @Override
    public void delete(Datadict model) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("数据字典不能删除，请设置为不可见.");
    }

    @Cacheable(value = CacheConstants.Datadict, key = "#id")
    @Override
    public Datadict get(int id) {
        return super.get(id);
    }

    @CacheEvict(value = CacheConstants.Datadict, allEntries = true)
    @Override
    public void add(Datadict model) {
        if (null != this.getByValue(model.getValue())) {
            throw new CoryException(ErrorCode.SAVE_ERROR, "code为" + model.getValue() + "的记录已经存在，请重新输入.");
        }
        super.add(model);
    }

    @CacheEvict(value = CacheConstants.Datadict, key = "#model.id", allEntries = true)
    @Override
    public void update(Datadict model) {
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
    public Datadict getByValue(String value) {
        Datadict dd = this.getDao().getByValue(value);
        fillOtherFields(dd);
        return dd;
    }

    @Cacheable(value = CacheConstants.Datadict, key = "'all-types'")
    public List<Datadict> getAllTypes() {
        List<Datadict> list = this.getDao().getAllTypes();
        fillOtherFields(list);
        return list;
    }

    @Cacheable(value = CacheConstants.Datadict, key = "'type-'.concat(#type)")
    public List<Datadict> getByType(Integer type) {
        List<Datadict> list = this.getDao().getByType(type);
        fillOtherFields(list);
        return list;
    }

    @Override
    protected void fillOtherFields(Datadict model) {
        if (null == model) {
            return;
        }
        if (null == model.getType() || model.getType() == 0) {
            model.setTypeDescription("ROOT(根类型)");
        } else {
            Datadict type = this.getDao().get(model.getType());
            model.setTypeDescription(type.getValue() + "(" + type.getDescription() + ")");
        }
        model.setShowableName(null != model.getShowable() && model.getShowable() ? "是" : "否");
    }
}
