package com.cory.db.processor;

import com.cory.db.annotations.Model;
import com.cory.db.config.CoryDbProperties;
import com.cory.db.jdbc.CoryDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.AnnotatedTypeScanner;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Created by Cory on 2021/2/9.
 */
@Slf4j
public class CoryDbInitializer {

    private CoryDbProperties coryDbProperties;
    private CoryDb coryDb;

    public CoryDbInitializer(CoryDb coryDb, CoryDbProperties coryDbProperties) {
        this.coryDb = coryDb;
        this.coryDbProperties = coryDbProperties;
    }

    @PostConstruct
    public void initialize() {
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
