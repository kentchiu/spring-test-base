package com.kentchiu.spring.base.dao;

public enum DatabaseType {
    HSQL {
        @Override
        public String getDriver() {
            return "org.hsqldb.jdbcDriver";
        }

    }, ORACLE {
        @Override
        public String getDriver() {
            return "oracle.jdbc.driver.OracleDriver";
        }

    }, MYSQL {
        public String getDriver() {
            return "oracle.jdbc.driver.OracleDriver";
        }

    }, POSTGRESQL {
        public String getDriver() {
            return "oracle.jdbc.driver.OracleDriver";
        }

    };

    public abstract String getDriver();
}