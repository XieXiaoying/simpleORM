package com.xiexy.orm.Executer;

import com.xiexy.orm.core.Execute;
import com.xiexy.orm.core.Processor;
import com.xiexy.orm.dbPool.DBPool;

import java.sql.SQLException;
import java.util.List;

public class Executer {
    private DBPool s;

    public Executer(DBPool pool) {
        this.s = pool;
    }

    public void closePool() throws SQLException {
        this.s.closePool();
    }

    public static Executer use(DBPool pool) {
        return new Executer(pool);
    }

    public Executer execute(String sql) {
        // TODO Auto-generated method stub
        Execute.executeSingelSQL(s, sql);
        return this;
    }

    public Executer executeBatch(List<String> sqls) {
        // TODO Auto-generated method stub
        Execute.executeSQLBatch(s, sqls);
        return this;
    }

    public Object executeAndReturn(String sql, Class c) {
        return Processor.RowSetToObject(Execute.executeSQLAndReturn(s, sql), c);
    }
}
