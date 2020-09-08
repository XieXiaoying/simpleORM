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
public class Queryer {
    private DBPool s;

    public Queryer(DBPool pool) {
        this.s = pool;
    }

    public static Queryer use(DBPool pool) {
        return new Queryer(pool);
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    public Object query(Object cond) throws ClassNotFoundAnnotation {
        RowValAndTable rowValAndTable = Processor.TableToRV(cond);
        return Processor.RowSetToObject(Execute.executeSQLAndReturn(s,
                buildQueryerSQL(rowValAndTable.getTableName(), rowValAndTable.getRowValue(), Relation.And)), cond.getClass());
    }

    public Object query(Object cond, Relation type) throws ClassNotFoundAnnotation {
        RowValAndTable rowValAndTable = Processor.TableToRV(cond);
        return Processor.RowSetToObject(Execute.executeSQLAndReturn(s,
                buildQueryerSQL(rowValAndTable.getTableName(), rowValAndTable.getRowValue(), type)), cond.getClass());
    }

    private String buildQueryerSQL(String table, RowVal rv, Relation type) {
        String sql = "select * from " + table;
        if (rv != null && rv.getRow().size() > 0) {
            List<TextVal> cond = rv.getRow();
            sql += " where ";
            for (int i = 0; i < cond.size(); i++) {
                sql += cond.get(i).getFiled() + " = '" + cond.get(i).getValue() + "'";
                if (i < cond.size() - 1) {
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
