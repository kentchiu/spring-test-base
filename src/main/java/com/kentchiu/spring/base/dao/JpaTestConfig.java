package com.kentchiu.spring.base.dao;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

public abstract class JpaTestConfig {

    public JpaTestConfig() {
        super();
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
        return transactionManager;
    }

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabase ds = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).addScript("classpath:/schema.ddl").build();
        return spyDataSource(ds);
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan(getPackagesToScan());
        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter() {
            {
                setShowSql(false);
                setGenerateDdl(false);
            }
        };
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        factoryBean.setJpaProperties(additionProperties());
        return factoryBean;
    }


    protected abstract String[] getPackagesToScan();

    @Bean(name = "entityManager")
    public EntityManager entityManager() {
        EntityManagerFactory emf = entityManagerFactoryBean().getObject();
        return emf.createEntityManager();
    }


    protected DataSource spyDataSource(DataSource dataSource) {
        return new DataSourceSpy(dataSource);
    }

    protected Properties additionProperties() {
        return new Properties() {
            {
                setProperty("hibernate.ejb.naming_strategy", org.hibernate.cfg.ImprovedNamingStrategy.class.getName());
            }
        };
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

}
