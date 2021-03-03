package com.cory.util;

import com.cory.context.CurrentUser;
import com.cory.model.BaseModel;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 对Class和Package的一些处理工具方法
 * 
 * @author Cory
 * 
 */
public class ModelUtil {

	public static <T extends BaseModel> void fillCreatorAndModifier(T model) {
		if (null == model) {
			return;
		}
		model.resetDateAndOperator(new Date(), CurrentUser.get().getId());
	}

	public static <T extends BaseModel> void fillCreatorAndModifier(List<T> list) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		list.forEach(model -> fillCreatorAndModifier(model));
	}
}
