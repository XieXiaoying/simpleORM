package com.xiexy.orm.core;

import com.xiexy.orm.dbPool.DBPool;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

public class Execute {
    /**
     * 执行单条sql语句
     *
     * @param s
     * 使用的线程池
     * @param sql
     * 要执行的sql语句
     */
    public static void executeSingelSQL(DBPool s, String sql) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = s.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);

        } catch (Exception e) {
            Logger.getLogger("logger").severe(e.toString());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
            if (conn != null) {
                try {
                    s.closeConnection(conn);
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
        }
        System.err.println(sql);
    }

    public static void executeSQLBatch(DBPool s, List<String> sqls) {
        Statement stmt = null;
        Savepoint savepoint = null;
        Connection conn = null;
        try {
            conn = s.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            /**
             * 保存点(SAVEPOINT)是事务处理过程中的一个标志，与回滚命令(ROLLBACK)结合使用，主要的用途是允许用户将某一段处理回滚而不必回滚整个事务。
             *
             * 如果定义了多个savepoint，当指定回滚到某个savepoint时，那么回滚操作将回滚这个savepoint后面的所有操作（即使后面可能标记了N个savepoint）。
             *
             * 例如，在一段处理中定义了五个savepoint，从第三个savepoint回滚，后面的第四、第五个标记的操作都将被回滚，如果不使用ROLLBACK TO savepoint_name而使用ROLLBACK，将会滚整个事务处理。
             *
             * 一旦执行了rollback那么savepoint的操作都将撤消，当然最后一定执行一次commit，否则所有的操作都是在缓存中进行的，不会真正的写入数据库中。
             *
             * */
            savepoint = conn.setSavepoint(); // 設定save point
            /*
             * 循环添加批次
             */
            for (String each : sqls) {
                stmt.addBatch(each);
            }
            stmt.executeBatch();
            /*
             * 如果没有出错
             */
            conn.commit();
            conn.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            Logger.getLogger("logger").warning(e.toString());
            try {
                conn.rollback(savepoint);
                conn.commit();
            } catch (SQLException e1) {
                Logger.getLogger("logger").severe(e1.toString());
            }
        } catch (Exception e) {
            Logger.getLogger("logger").severe(e.toString());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
            if (conn != null) {
                try {
                    s.closeConnection(conn);
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
        }
        System.err.println("update count : " + sqls.size());
    }


    /**
     * 执行单条sql语句，并返回得到的值
     * @param s
     * 线程池
     * @param sql
     * 要执行的sql语句
     * @return
     * 得到的个数
     */
    public static int executeSQLSingleAndReturnCount(DBPool s, String sql) {
        Statement stmt = null;
        Connection conn = null;
        ResultSet resultSet = null;
        int res = 0;
        try {
            conn = s.getConnection();
            stmt = conn.createStatement();
            /**
             * ResultSet 对象维持着一个指向当前行的指针，最初，这个指针指向第一行之前。Result类的next方法使这个指针指向第一行之前。
             * next方法使这个指针向下移动一行。因此，第一次使用next指针将指向这个指针的第一行。next方法返回true，则证明结果集还存
             * 在下一条记录，且指针也已经指向这一条记录。
             *
             * */
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                // 在数据遍历的过程中，数据库一直处于连接状态中，性能低且不安全
                // 返回第一列的值
                /**
                 * int getInt(int columnIndex)
                 *            throws SQLException
                 * Retrieves the value of the designated column in the current row of this ResultSet object as an int in the Java programming language.
                 * Parameters:
                 * columnIndex - the first column is 1, the second is 2, ...
                 * Returns:
                 * the column value; if the value is SQL NULL, the value returned is 0
                 *
                 * */
                // 这里的执行语句是写死的，select count(*) from ...,所以第一列数据就是结果集的条数
                res = resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
            if (conn != null) {
                try {
                    s.closeConnection(conn);
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
        }
        System.err.println(sql);
        return res;
    }

    /**
     * 执行sql并返回结果
     * @param s
     * 使用的线程池
     * @param sql
     * 执行的sql语句
     * @return
     * 返回结果
     */
    public static RowSet executeSQLAndReturn(DBPool s, String sql) {
        Statement stmt = null;
        Connection conn = null;
        ResultSet resultSet = null;
        CachedRowSet rowset = null;
        try {
            conn = s.getConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            // 离线版的RowSet，关闭了连接之后仍然可以使用
            rowset = RowSetProvider.newFactory().createCachedRowSet();
            // 将ResultSet封装成CachedRowSet
            rowset.populate(resultSet);
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
            if (conn != null) {
                try {
                    s.closeConnection(conn);
                } catch (SQLException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
        }
        System.err.println(sql);
        return rowset;
    }
}
