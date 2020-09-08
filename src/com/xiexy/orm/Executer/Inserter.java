package com.xiexy.orm.Executer;

import com.xiexy.orm.core.Execute;
import com.xiexy.orm.core.Processor;
import com.xiexy.orm.dbPool.DBPool;
import com.xiexy.orm.define.RowVal;
import com.xiexy.orm.define.RowValAndTable;
import com.xiexy.orm.define.TextVal;
import com.xiexy.orm.exception.BuildSqlException;
import com.xiexy.orm.exception.ClassNotFoundAnnotation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Inserter {
    private DBPool s;

    public Inserter(DBPool pool) {
        this.s = pool;
    }

    public static Inserter use(DBPool pool) {
        return new Inserter(pool);
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    /**
     * 插入注解类
     * @param object
     * 插入的对象
     * @return
     * 接着使用
     */
    public Inserter insert(Object object) throws BuildSqlException, ClassNotFoundAnnotation {
        RowValAndTable rowValAndTable = Processor.TableToRV(object);
        if (rowValAndTable != null) {
            Execute.executeSingelSQL(s, buildInserterSQL(rowValAndTable.getRowValue(), rowValAndTable.getTableName()));
        }
        return this;
    }

    /**
     * 插入多个对象
     * @param objects
     * 插入的对象们
     * @return
     * 接着使用
     */
    public Inserter insert(Collection<Object> objects) throws BuildSqlException, ClassNotFoundAnnotation {
        for (Object object : objects) {
            RowValAndTable rowValAndTable = Processor.TableToRV(object);
            if (rowValAndTable != null) {
                Execute.executeSingelSQL(s, buildInserterSQL(rowValAndTable.getRowValue(), rowValAndTable.getTableName()));
            }
        }
        return this;
    }

    /**
     * 批量插入注解类
     * @param objects
     * 按顺序插入的对象
     * @return
     * 接着使用
     */
    public Inserter insertBatch(List<Object> objects) throws BuildSqlException, ClassNotFoundAnnotation {
        List<String> sqls = new ArrayList<>();
        for (Object object : objects) {
            RowValAndTable rowValAndTable = Processor.TableToRV(object);
            if (rowValAndTable != null)
                sqls.add(buildInserterSQL(rowValAndTable.getRowValue(), rowValAndTable.getTableName()));
        }
        Execute.executeSQLBatch(s, sqls);
        return this;
    }

    private String buildInserterSQL(RowVal rv, String table) throws BuildSqlException {
        if (rv == null || rv.getRow().isEmpty())
            throw new BuildSqlException();
        List<TextVal> values = rv.getRow();
        String sql = "insert IGNORE into " + table;
        String sqlset = " (";
        String sqlvalues = "values(";
        int count = values.size();
        for (int i = 0; i < count; i++) {
            sqlset += values.get(i).getFiled();
            sqlvalues += "'" + values.get(i).getValue() + "'";
            if (i < count - 1) {
                sqlset += ",";
                sqlvalues += ",";
            }
        }
        sqlset += ") ";
        sqlvalues += ");";
        sql = sql + sqlset + sqlvalues;
        return sql;
    }
}
