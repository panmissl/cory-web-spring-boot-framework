package com.cory.db.config;

import com.cory.db.jdbc.CoryDb;
import com.cory.db.jdbc.FakeCoryDataSource;
import com.cory.db.jdbc.FakeCoryDb;
import com.cory.db.processor.CoryDbDaoProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 使用前，在application.properties文件里配置数据库信息：cory.db.enable = false
 * <br />
 * Created by Cory on 2021/2/9.
 */
@Configuration
@ConditionalOnProperty(prefix = Constant.PREFIX, name = Constant.ENABLE, havingValue = "false")
@EnableConfigurationProperties(CoryDbProperties.class)
public class CoryDbAutoConfigurationWithoutDataSource {

    @Bean
    public DataSource dataSource() {
        return new FakeCoryDataSource();
    }

    @Bean
    public CoryDb coryDb() {
        return new FakeCoryDb();
    }

    @Bean
    public CoryDbDaoProcessor coryDbDaoProcessor() {
        return new CoryDbDaoProcessor();
    }
}
