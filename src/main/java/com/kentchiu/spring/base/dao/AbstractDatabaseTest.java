package com.kentchiu.spring.base.dao;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class})
public abstract class AbstractDatabaseTest {

    protected JdbcTemplate jdbcTemplate;
    protected DataSource dataSource;


    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Before
    public void setUp() throws Exception {
        DatabaseDataSourceConnection connection = new DatabaseDataSourceConnection(this.dataSource);
        DatabaseConfig config = connection.getConfig();
        config.setProperty("http://www.dbunit.org/properties/datatypeFactory", new HsqldbDataTypeFactory());
        CsvDataSet dataSet1 = new CsvDataSet(getCsvFolder().toFile());
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet1);
    }

    protected Path getCsvFolder() {
        URL url = this.getClass().getResource(this.getClass().getSimpleName());
        Path path;
        try {
            if (url == null) {
                throw new IllegalStateException("CSV files not exist : " + this.getClass().getSimpleName());
            }
            path = Paths.get(url.toURI());
            if (!Files.exists(path)) {
                throw new IllegalStateException("CSV files not exist : " + path);
            }
            if (!Files.isDirectory(path)) {
                throw new IllegalStateException("CSV files not exist : " + path);
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("can't found csv folder at : " + url);
        }
        return path;
    }


}
