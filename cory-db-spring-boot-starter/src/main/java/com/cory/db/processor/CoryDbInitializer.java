package com.cory.db.processor;

import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.CoryDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbInitializer  implements InitializingBean {

    private CoryDbProperties coryDbProperties;
    private CoryDb coryDb;

    public CoryDbInitializer(CoryDb coryDb, CoryDbProperties coryDbProperties) {
        this.coryDb = coryDb;
        this.coryDbProperties = coryDbProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /* 不在这里初始化，初始化数据手动进行
        AnnotatedTypeScanner scanner = new AnnotatedTypeScanner(true, Model.class);
        Set<Class<?>> set = scanner.findTypes(coryDbProperties.getModelPackages());
        if (CollectionUtils.isEmpty(set)) {
            log.info("no model found in packages: {}", coryDbProperties.getModelPackages());
            return ;
        }

        for (Class<?> cls : set) {
            Model model = cls.getAnnotation(Model.class);
            if (null == model) {
                continue;
            }
            String[] sqls = model.initDataSql();
            if (null == sqls || sqls.length == 0) {
                continue;
            }
            for (String sql : sqls) {
                if (StringUtils.isBlank(sql)) {
                    continue;
                }
                try {
                    coryDb.executeSql(sql);
                } catch (Throwable t) {
                    log.info("initialize data error, sql: {}, error: {}", sql, t.getMessage());
                }
                log.info("initialize data: " + sql);
            }
        }

        log.info("cory db initialize finish.");
        */
    }
}
