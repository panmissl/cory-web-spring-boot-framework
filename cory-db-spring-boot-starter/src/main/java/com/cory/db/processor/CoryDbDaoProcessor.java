package com.cory.db.processor;

import com.cory.db.annotations.Dao;
import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.CoryDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.util.AnnotatedTypeScanner;

import java.util.Set;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbDaoProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public CoryDbDaoProcessor() {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        log.info("cory db register bean definition start");

        AnnotatedTypeScanner scanner = new AnnotatedTypeScanner(true, Dao.class);
        Set<Class<?>> set = scanner.findTypes("com.cory.dao");
        if (CollectionUtils.isEmpty(set)) {
            log.info("no dao found in packages: com.cory.dao");
            return;
        }

        for (Class<?> cls : set) {
            RootBeanDefinition bd = (RootBeanDefinition) BeanDefinitionBuilder.rootBeanDefinition(cls)
                    .setScope(ConfigurableBeanFactory.SCOPE_SINGLETON)
                    .setLazyInit(true)
                    .getBeanDefinition();

            //参考：org.mybatis.spring.mapper.MapperScannerConfigurer.Scanner#doScan
            //设置bean class后，需要设置属性
            bd.setBeanClass(CoryDaoFactoryBean.class);
            bd.getPropertyValues().add("cls", cls);
            bd.getPropertyValues().add("ctx", applicationContext);

            beanDefinitionRegistry.registerBeanDefinition(StringUtils.capitalize(cls.getSimpleName()), bd);
        }

        log.info("cory db register bean definition finish, registered {} DAO", set.size());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        //do nothing
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
