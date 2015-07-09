package com.kentchiu.spring.base.dao;

import com.google.common.collect.Iterables;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Map;

public class DbunitExporter {

    private Connection connection;


    public DbunitExporter(DatabaseType db, String url, String username, String password) {
        try {
            Class.forName(db.getDriver());
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static Path makeTestDataFileSubPath(Class<?> testClass) {
        String className = testClass.getSimpleName();
        String packageName = testClass.getPackage().getName();
        Path result = Paths.get(packageName.replaceAll("\\.", "/"), className + "-testdata.xml");
        return result;
    }

    private IDatabaseConnection getDbunitConnection() throws DatabaseUnitException {
        return new DatabaseConnection(connection);
    }


    /**
     * @param tableNames
     * @param columnNameMapping 用來將原來的 column name 映射成另一個名稱
     * @return
     * @throws SQLException
     * @throws DataSetException
     */
    public Path exportSchema(String[] tableNames, Map<String, String> columnNameMapping) throws SQLException, DataSetException {
        StringBuilder sb = new StringBuilder();
        for (String tableName : tableNames) {
            String sql = "select  * from " + tableName;
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            sb.append("CREATE TABLE IF NOT EXISTS " + tableName).append("\n");
            sb.append("(").append("\n");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String key = tableName + "." + metaData.getColumnName(i);
                if (Iterables.contains(columnNameMapping.keySet(), key)) {
                    sb.append("\t").append(columnNameMapping.get(key));
                } else {
                    String type = getType(metaData, i);
                    sb.append("\t").append(metaData.getColumnName(i)).append(" ").append(type);
                }
                if (i != metaData.getColumnCount()) {
                    sb.append(",").append("\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append(");").append("\n").append("\n");
        }

        try {
            Path schema = Files.createTempFile("dbunit-schema", "ddl");
            Files.write(schema, sb.toString().getBytes());
            return schema;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("export schema fail", e);
        }
    }

    private String getType(ResultSetMetaData metaData, int i) throws SQLException {
        int columnType = metaData.getColumnType(i);
        String type;
        switch (columnType) {
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
            case Types.VARCHAR:
                type = "varchar(255)";
                break;
            case Types.INTEGER:
            case Types.NUMERIC:
            case Types.BIGINT:
            case Types.REAL:
                if (metaData.getScale(i) == 0) {
                    type = "integer";
                } else {
                    type = "float";
                }
                break;
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.DOUBLE:
                type = "float";
                break;
            case Types.DATE:
            case Types.TIMESTAMP:
                type = "datetime";
                break;
            case Types.BIT:
                type = "boolean";
                break;
            default:
                System.err.println("UNKNOWN COLUMN TYPE:" + columnType);
                type = "UNKNOWN COLUMN TYPE";
        }
        return type;
    }
}
