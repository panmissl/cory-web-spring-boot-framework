package com.cory.db.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.cory.db.jdbc.CoryDb;
import com.cory.db.processor.CoryDbChecker;
import com.cory.db.processor.CoryDbDaoProcessor;
import com.cory.db.processor.CoryDbInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 使用前，在application.properties文件里配置数据库信息：spring.datasource.username、spring.datasource.password、spring.datasource.name
 * <br />
 * Created by Cory on 2021/2/9.
 */
@Configuration
@EnableConfigurationProperties(CoryDbProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass(DruidDataSourceAutoConfigure.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class CoryDbAutoConfiguration {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public CoryDb coryDb(JdbcTemplate jdbcTemplate) {
        return new CoryDb(jdbcTemplate);
    }

    @Bean
    public CoryDbDaoProcessor coryDbDaoProcessor() {
        return new CoryDbDaoProcessor();
    }

    @Bean
    public CoryDbChecker coryDbChecker(CoryDbProperties coryDbProperties, CoryDb coryDb, @Value("${spring.datasource.name") String database) {
        return new CoryDbChecker(coryDb, coryDbProperties, database);
    }

    @Bean
    @DependsOn("coryDbChecker")
    public CoryDbInitializer coryDbInitializer(CoryDb coryDb, CoryDbProperties coryDbProperties) {
        return new CoryDbInitializer(coryDb, coryDbProperties);
    }
}
