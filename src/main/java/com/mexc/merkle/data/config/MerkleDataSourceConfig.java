package com.mexc.merkle.data.config;

import com.mexc.merkle.data.util.AwsSecretManager;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@Import(MybatisAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class)
@MapperScan(basePackages = "com.mexc.merkle.data.mapper", sqlSessionFactoryRef = "merkleSqlSessionFactory")
@EnableTransactionManagement
@Slf4j
public class MerkleDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("classpath*:mapper/*.xml")
    private Resource[] mapperLocations;
    @Value("${aws.region}")
    private String awsRegion;

    @Bean("merkleDataSource")
    public DataSource getDataSource() {
        AwsSecretManager awsSecretManager = new AwsSecretManager(awsRegion);
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        String secretUrl = awsSecretManager.getSecretField(url);
        String secretUserName = awsSecretManager.getSecretField(username);
        String secretPassword = awsSecretManager.getSecretField(password);
        log.info("secretUrl:{},value:{}", secretUrl,awsSecretManager.getValue(secretUrl));
        return dataSourceBuilder.url(StringUtils.hasText(secretUrl) ? awsSecretManager.getValue(secretUrl) : url)
                .username(StringUtils.hasText(secretUserName) ? awsSecretManager.getValue(secretUserName) : username)
                .password(StringUtils.hasText(secretPassword) ? awsSecretManager.getValue(secretPassword) : password)
                .type(HikariDataSource.class).build();
    }

    @Bean("merkleSqlSessionFactory")
    public SqlSessionFactoryBean createSqlSessionFactoryBean(@Qualifier("merkleDataSource") DataSource ds) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(ds);
        bean.setTypeAliasesPackage("com.mexc.merkle.data.mapper");
        bean.setMapperLocations(mapperLocations);
        return bean;
    }

}
