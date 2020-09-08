package com.xiexy.orm.Executer;

import com.xiexy.orm.core.Execute;
import com.xiexy.orm.core.Processor;
import com.xiexy.orm.dbPool.DBPool;
import com.xiexy.orm.define.RowVal;
import com.xiexy.orm.define.RowVal.Relation;
import com.xiexy.orm.define.RowValAndTable;
import com.xiexy.orm.define.TextVal;
import com.xiexy.orm.exception.ClassNotFoundAnnotation;

import java.sql.SQLException;
import java.util.List;

public class Counter {
    private DBPool s;

    public Counter(DBPool pool) {
        this.s = pool;
    }

    public static Counter use(DBPool pool) {
        return new Counter(pool);
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    /**
     * 获取个数
     * @param cond
     * 与表对应的对象
     * @return
     * 返回个数
     */
    public int getCountOf(Object cond) throws ClassNotFoundAnnotation {
        return getCountOf(cond, Relation.And);
    }

    public int getCountOf(Object cond, Relation type) throws ClassNotFoundAnnotation {
        RowValAndTable rowValAndTable = Processor.TableToRV(cond);
        return Execute.executeSQLSingleAndReturnCount(s, buildCounterSQL(rowValAndTable.getTableName(),
                rowValAndTable.getRowValue(), type));
    }

    /**
     * 创建counter的sql语句
     *
     * @param table
     * 表名称
     * @param rv
     * rv
     * @param type
     * 类型
     * @return
     * 返回sql
     */
    private String buildCounterSQL(String table, RowVal rv, Relation type) {
        String sql = "select count(*) from " + table;
        if (rv != null && rv.getRow().size() > 0) {
            List<TextVal> tv = rv.getRow();
            sql += " where ";
            for (int i = 0; i < tv.size(); i++) {
                sql += tv.get(i).getFiled() + " = '" + tv.get(i).getValue() + "'";
                if (i < tv.size() - 1) {
                    if (type == Relation.And) {
                        sql += " and ";
                    } else {
                        sql += " or ";
                    }
                }
            }
        }
        return sql;
    }
}
