package com.xiexy.orm.Executer;

import java.sql.SQLException;
import java.util.List;
import com.xiexy.orm.core.Execute;
import com.xiexy.orm.core.Processor;
import com.xiexy.orm.dbPool.DBPool;
import com.xiexy.orm.define.RowVal;
import com.xiexy.orm.define.RowVal.Relation;
import com.xiexy.orm.define.RowValAndTable;
import com.xiexy.orm.define.TextVal;
import com.xiexy.orm.exception.ClassNotFoundAnnotation;
public class Deleter {
    private DBPool s;

    public Deleter(DBPool pool) {
        this.s = pool;
    }

    public static Deleter use(DBPool pool) {
        return new Deleter(pool);
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    /**
     * 删除
     * @param cond
     * 条件对象
     * @return
     * 可以接着使用
     */
    public Deleter delete(Object cond) throws ClassNotFoundAnnotation {
        RowValAndTable rv = Processor.TableToRV(cond);
        Execute.executeSingelSQL(s, buildDeleteSQL(rv.getTableName(), rv.getRowValue(), Relation.And));
        return this;
    }

    /**
     * 删除
     * @param cond
     * 条件对象
     * @param type
     * 条件
     * @return
     * 可以接着使用
     */
    public Deleter delete(Object cond, Relation type) throws ClassNotFoundAnnotation {
        RowValAndTable rv = Processor.TableToRV(cond);
        Execute.executeSingelSQL(s, buildDeleteSQL(rv.getTableName(), rv.getRowValue(), type));
        return this;
    }

    private String buildDeleteSQL(String table, RowVal rv, Relation type) {
        String sql = "delete from " + table;
        if (rv != null && rv.getRow().size() > 0) {
            List<TextVal> conds = rv.getRow();
            sql += " where ";
            for (int i = 0; i < conds.size(); i++) {
                sql += conds.get(i).getFiled() + " = '" + conds.get(i).getValue() + "'";
                if (i < conds.size() - 1) {
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
