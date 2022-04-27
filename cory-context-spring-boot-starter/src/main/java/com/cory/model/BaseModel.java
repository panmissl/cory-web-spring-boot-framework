package com.cory.model;

import lombok.Data;

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
     * 支持的有：Start(包含)、End(不包含)、In、Like、LikeLeft、LikeRight、NotIn，NotLike、NotLikeLeft、NotLikeRight、NotEq(不等)
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
     DemoModel demoModel = DemoModel.builder().name("zhangsan").age(18).status(1).birthday(new Date()).build();
     demoModel.addStartEndFilterField("birthday", DateUtils.parseDate("2021-01-01"), DateUtils.parseDate("2023-05-18"));
     demoModel.addInFilterField("status", Lists.newArrayList(3, 4, 5));
     demoModel.addNotInFilterField("status", Lists.newArrayList(15, 16, 17));
     demoModel.addLikeFilterField("name", "z", true, true);
     demoModel.addLikeFilterField("name", "g", true, false);
     demoModel.addLikeFilterField("name", "h", false, true);
     demoModel.addNotLikeFilterField("name", "fffff", true, true);
     demoModel.addNotLikeFilterField("name", "bbbb", true, false);
     demoModel.addNotLikeFilterField("name", "qqqq", false, true);
     demoModel.addNotEqFilterField("age", 3333);
     * </pre>
     */
    protected Map<String, Object> filterFieldMap = new HashMap<>();

    /**
     * 参考filterFieldMap的注释，给一个字段添加Start和End的过滤条件。min和max也是一样，所以都用这个方法
     * @param fieldName 字段名。比如：createTime
     * @param startValue start值，包含。sql会写成：fieldName >= #{startValue}，如果为空则不添加
     * @param endValue end值，不包含。sql会写成：fieldName < #{endValue}，如果为空则不添加
     */
    public void addStartEndFilterField(String fieldName, Object startValue, Object endValue) {
        if (null != startValue) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_START, startValue);
        }
        if (null != endValue) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_END, endValue);
        }
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加in的过滤条件
     * @param fieldName 字段名。比如：status
     * @param list 要in的列表，可以是基本类型和字符串类型，或者枚举类型。其它的会使用toString方法转换，然后添加单引号
     */
    public void addInFilterField(String fieldName, List<? extends Object> list) {
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_IN, list);
    }

    /**
     * 参考addInFilterField的注释，给一个字段添加not in的过滤条件
     * @param fieldName
     * @param list
     */
    public void addNotInFilterField(String fieldName, List<? extends Object> list) {
        filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_IN, list);
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加like的过滤条件
     * @param fieldName 字段名。比如：name
     * @param value 要like的值，使用toString转为字符串。
     * @param leftLike 是否左like
     * @param rightLike 是否右like
     */
    public void addLikeFilterField(String fieldName, Object value, boolean leftLike, boolean rightLike) {
        if (leftLike && rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE, value);
        } else if (leftLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE_LEFT, value);
        } else if (rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_LIKE_RIGHT, value);
        }
    }

    /**
     * 参考addLikeFilterField的注释，给一个字段添加not like的过滤条件
     * @param fieldName
     * @param value
     * @param leftLike
     * @param rightLike
     */
    public void addNotLikeFilterField(String fieldName, Object value, boolean leftLike, boolean rightLike) {
        if (leftLike && rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE, value);
        } else if (leftLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE_LEFT, value);
        } else if (rightLike) {
            filterFieldMap.put(fieldName + FILTER_FIELD_POSTFIX_NOT_LIKE_RIGHT, value);
        }
    }

    /**
     * 参考filterFieldMap的注释，给一个字段添加不等于的过滤条件
     * @param fieldName 字段名。比如：age
     * @param value 可以是基本类型和字符串类型，或者枚举类型。其它的会使用toString方法转换，然后添加单引号
     */
    public void addNotEqFilterField(String fieldName, Object value) {
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
