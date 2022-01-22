package com.cory.web.binding;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 数据绑定初始化类
 */
@Configuration
public class BindingInitializer extends ConfigurableWebBindingInitializer {

	/**
	 * 初始化数据绑定
	 */
	@Override
	public void initBinder(WebDataBinder webDataBinder) {
		super.initBinder(webDataBinder);

		webDataBinder.setAutoGrowNestedPaths(true);
		webDataBinder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);

		webDataBinder.registerCustomEditor(Date.class, new DateTypeEditor());
		webDataBinder.registerCustomEditor(Timestamp.class, new TimestampTypeEditor());
	}
}
