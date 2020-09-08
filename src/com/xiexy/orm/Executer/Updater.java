package com.xiexy.orm.Executer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.xiexy.orm.core.Execute;
import com.xiexy.orm.core.Processor;
import com.xiexy.orm.dbPool.DBPool;
import com.xiexy.orm.define.RowVal;
import com.xiexy.orm.define.RowVal.Relation;
import com.xiexy.orm.define.RowValAndTable;
import com.xiexy.orm.define.TextVal;
import com.xiexy.orm.exception.ClassNotFoundAnnotation;
import com.xiexy.orm.exception.BuildSqlException;
public class Updater {
    private DBPool s;

    public Updater(DBPool pool) {
        this.s = pool;
    }

    public static Updater use(DBPool pool) {
        return new Updater(pool);
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    /**
     * 更新，使用注解
     *
     * @param update
     * 要更新的对象
     * @param cond
     * 条件对象
     * @return
     * 接着使用
     */
    public Updater update(Object update, Object cond) throws ClassNotFoundAnnotation, BuildSqlException {
        RowValAndTable u = Processor.TableToRV(update);
        RowValAndTable c = Processor.TableToRV(cond);
        Execute.executeSingelSQL(s, buildUpdaterSQL(u.getTableName(), u.getRowValue(), c.getRowValue(), Relation.And));
        return this;
    }

    public Updater update(Object update, Object cond, Relation type) throws ClassNotFoundAnnotation, BuildSqlException {
        RowValAndTable u = Processor.TableToRV(update);
        RowValAndTable c = Processor.TableToRV(cond);
        Execute.executeSingelSQL(s, buildUpdaterSQL(u.getTableName(), u.getRowValue(), c.getRowValue(), type));
        return this;
    }

    /**
     * 批量更新,默认类型
     * @param update
     * 批量更新的对象
     * @param cond
     * 条件对象
     * @return
     * 接着使用
     */
    public Updater updateBatch(List<Object> update, List<Object> cond) throws ClassNotFoundAnnotation, BuildSqlException {
        // 构造sql
        List<String> sqls = new ArrayList<>();
        for (int i = 0; i < update.size(); i++) {
            RowValAndTable u = Processor.TableToRV(update.get(i));
            RowValAndTable c = Processor.TableToRV(cond.get(i));
            if (i < cond.size()) {
                String sql = buildUpdaterSQL(u.getTableName(), u.getRowValue(), c.getRowValue(), Relation.And);
                sqls.add(sql);
            } else {
                // 没有条件
                String sql = buildUpdaterSQL(u.getTableName(), u.getRowValue(), null, Relation.And);
                sqls.add(sql);
            }
        }
        Execute.executeSQLBatch(s, sqls);
        return this;
    }

    /**
     * 批量更新，自定义类型
     * @param update
     * 对象
     * @param cond
     * 条件
     * @param type
     * 类型
     * @return
     * 接着使用
     */
    public Updater updateBatch(List<Object> update, List<Object> cond, Relation type) throws ClassNotFoundAnnotation, BuildSqlException {
        // 构造sql
        List<String> sqls = new ArrayList<>();
        for (int i = 0; i < update.size(); i++) {
            RowValAndTable u = Processor.TableToRV(update.get(i));
            RowValAndTable c = Processor.TableToRV(cond.get(i));
            if (i < cond.size()) {
                String sql = buildUpdaterSQL(u.getTableName(), u.getRowValue(), c.getRowValue(), type);
                sqls.add(sql);
            } else {
                // 没有条件
                String sql = buildUpdaterSQL(u.getTableName(), u.getRowValue(), null, type);
                sqls.add(sql);
            }
        }
        Execute.executeSQLBatch(s, sqls);
        return this;
    }

    private String buildUpdaterSQL(String table, RowVal rv, RowVal rvCond, Relation type) throws BuildSqlException {
        if (rv == null || rv.getRow().isEmpty())
            throw new BuildSqlException();
        List<TextVal> update = rv.getRow();
        String sql = "update " + table + " set ";
        for (int i = 0; i < update.size(); i++) {
            sql += update.get(i).getFiled() + " = '" + update.get(i).getValue() + "'";
            if (i < update.size() - 1) {
                sql += ", ";
            }
        }
        if (rvCond != null && rvCond.getRow().size() > 0) {
            List<TextVal> cond = rvCond.getRow();
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
