package com.cory.model;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cory.constant.Constants.*;

/**
 * Created by Cory on 2017/5/10.
 */
@Data
public abstract class BaseModel implements Serializable {

    private static final long serialVersionUID = -3571217121353176763L;

    private Integer id;
    private Integer creator;
    private Integer modifier;
    private Date createTime = new Date();
    private Date modifyTime = new Date();
    private Boolean isDeleted = false;

    /**
     * 显示字段，比如对于status，一般都存的Name，所以页面显示时需要显示text字段。这样就不用在Model类里加一些不必要的显示字段。
     * <br />
     * <br />
     * 使用：直接用model.getRenderFieldMap().put(name, value)即可。然后在页面上，JS里直接使用。
     */
    protected Map<String, String> renderFieldMap = new HashMap<>();

    /**
     * 过滤字段，比如对于operateDate，是一个时间段，会存两个字段：operateDateStart, operateDateEnd。会在请求时自动解析并设置到此字段里，sql里直接写变量名即可
     * <br />
     * <br />
     * 支持的有：Start(包含)、End(不包含)、In、Like、LikeLeft、LikeRight、NotIn，NotLike、NotLikeLeft、NotLikeRight、NotEq(不等)、IsNull、NotNull
     * <br />
     * <br />
     * 使用：通过本类中对应的addXxxFilterField方法添加
     * <br />
     * <br />
     * 用法示例：
     * <br />
     * <br />
     * <pre>
     *
     DemoModel demoModel = DemoModel.builder().name("zhangsan").age(18).status(1).birthDay(new Date()).build();
     demoModel.addStartEndFilterField("birthDay", DateUtils.parseDate("2021-01-01"), DateUtils.parseDate("2023-05-18"), true, false);
     demoModel.addStartEndFilterField("age", 18, 32, true, false);
     demoModel.addInFilterField("status", Lists.newArrayList(3, 4, 5));
     demoModel.addNotInFilterField("payStatus", Lists.newArrayList(15, 16, 17));
     demoModel.addLikeFilterField("name", "z", true, true);
     demoModel.addLikeFilterField("shortDesc", "g", true, false);
     demoModel.addLikeFilterField("name", "h", false, true);
     demoModel.addNotLikeFilterField("name", "fffff", true, true);
     demoModel.addNotLikeFilterField("name", "bbbb", true, false);
     demoModel.addNotLikeFilterField("name", "qqqq", false, true);
     demoModel.addNotEqFilterField("age", 3333);
     demoModel.addIsNullFilterField("status);
     demoModel.addNotNullFilterField("status);
     * </pre>
     */
    protected Map<String, Object> filterFieldMap = new HashMap<>();

    /**
     * 参考addStartEndFilterField的注释，给一个字段添加Start的过滤条件。min也是一样，所以都用这个方法
     * @param fieldName 字段名。比如：createTime，驼峰形式
     * @param value start值。sql会写成：fieldName > #{value}，如果为空则不添加，相当于可选的sql
     * @param inclusive 是否包含。如果包含sql会写成：fieldName >= #{value}，否则是：filedName > #{value}
     */
    public void addStartFilterField(String fieldName, Object value, boolean inclusive) {
        addStartEndFilterField(fieldName, value, null, inclusive, false);
    }

    /**
     * 参考addStartEndFilterField的注释，给一个字段添加End的过滤条件。max也是一样，所以都用这个方法
     * @param fieldName 字段名。比如：createTime，驼峰形式
     * @param value end值。sql会写成：fieldName < #{value}，如果为空则不添加，相当于可选的sql
     * @param inclusive 是否包含。如果包含sql会写成：fieldName <= #{value}，否则是：filedName < #{value}
     */
    public void addEndFilterField(String fieldName, Object value, boolean inclusive) {
        addStartEndFilterField(fieldName, null, value, false, inclusive);
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加Start和End的过滤条件。min和max也是一样，所以都用这个方法
     * @param fieldName 字段名。比如：createTime，驼峰形式
     * @param startValue start值。sql会写成：fieldName > #{startValue}，如果为空则不添加，相当于可选的sql
     * @param endValue end值。sql会写成：fieldName < #{endValue}，如果为空则不添加，相当于可选的sql
     * @param startInclusive start是否包含。如果包含sql会写成：fieldName >= #{startValue}，否则是：filedName > #{startValue}
     * @param endInclusive end是否包含。如果包含sql会写成：fieldName <= #{endValue}，否则是：filedName < #{endValue}
     */
    public void addStartEndFilterField(String fieldName, Object startValue, Object endValue, boolean startInclusive, boolean endInclusive) {
        if (null != startValue) {
            if (startInclusive) {
                filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_START_INCLUSIVE, startValue);
            } else {
                filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_START_EXCLUSIVE, startValue);
            }
        }
        if (null != endValue) {
            if (endInclusive) {
                filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_END_INCLUSIVE, endValue);
            } else {
                filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_END_EXCLUSIVE, endValue);
            }
        }
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加in的过滤条件。为空则不添加，相当于可选的sql
     * @param fieldName 字段名。比如：status，驼峰形式
     * @param list 要in的列表，可以是基本类型和字符串类型，或者枚举类型。其它的会使用toString方法转换，然后添加单引号
     */
    public void addInFilterField(String fieldName, List<? extends Object> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_IN, list);
    }

    /**
     * 参考addInFilterField的注释，给一个字段添加not in的过滤条件。为空则不添加，相当于可选的sql
     * @param fieldName 字段名。比如：status，驼峰形式
     * @param list
     */
    public void addNotInFilterField(String fieldName, List<? extends Object> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_IN, list);
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加is null的过滤条件
     * @param fieldName 字段名。比如：status，驼峰形式
     */
    public void addIsNullFilterField(String fieldName) {
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_IS_NULL, 1);
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加is not null的过滤条件
     * @param fieldName 字段名。比如：status，驼峰形式
     */
    public void addNotNullFilterField(String fieldName) {
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_NULL, 1);
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加like的过滤条件
     * @param fieldName 字段名。比如：name，驼峰形式
     * @param value 要like的值，使用toString转为字符串。为空则不添加，相当于可选的sql
     * @param leftLike 是否左like
     * @param rightLike 是否右like
     */
    public void addLikeFilterField(String fieldName, Object value, boolean leftLike, boolean rightLike) {
        if (null == value) {
            return;
        }
        if (leftLike && rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE_BOTH, value);
        } else if (leftLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE_LEFT, value);
        } else if (rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE_RIGHT, value);
        }
    }

    /**
     * 参考addLikeFilterField的注释，给一个字段添加not like的过滤条件，value为空则不添加，相当于可选的sql
     * @param fieldName 字段名。比如：name，驼峰形式
     * @param value
     * @param leftLike
     * @param rightLike
     */
    public void addNotLikeFilterField(String fieldName, Object value, boolean leftLike, boolean rightLike) {
        if (null == value) {
            return;
        }
        if (leftLike && rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE_BOTH, value);
        } else if (leftLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT, value);
        } else if (rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT, value);
        }
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加不等于的过滤条件
     * @param fieldName 字段名。比如：age，驼峰形式
     * @param value 可以是基本类型和字符串类型，或者枚举类型。其它的会使用toString方法转换，然后添加单引号，value为空则不添加，相当于可选的sql
     */
    public void addNotEqFilterField(String fieldName, Object value) {
        if (null == value) {
            return;
        }
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_EQ, value);
    }

    public void resetDateAndOperator(Date date, Integer operator) {
        if (null == this.id || this.id <= 0) {
            this.createTime = date;
            this.creator = operator;
        }
        this.modifyTime = date;
        this.modifier = operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseModel baseModel = (BaseModel) o;

        return id != null ? id.equals(baseModel.id) : baseModel.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
