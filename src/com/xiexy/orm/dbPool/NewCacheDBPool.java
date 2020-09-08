package com.xiexy.orm.dbPool;

import com.xiexy.orm.tools.ConfSetting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class NewCacheDBPool implements DBPool {
    private String user;
    private String password;
    private String url;
    private String driver;

    private ScheduledExecutorService service;
    /*
     * 连接池
     */
    private BlockingQueue<Connection> connections = new LinkedBlockingQueue<>();
    private int connectionPoolSize = 10;
    private int checkTimeout = 5; //second
    private int checkScheduleTime = 60 * 5; //second

    // 默认加载下级conf目录下面的db.conf文件
    public NewCacheDBPool() throws IOException, ClassNotFoundException {
        this("./conf/db.conf");
    }

    public NewCacheDBPool(String file) throws IOException, ClassNotFoundException {

        Map<String, String> conf = ConfSetting.create(file).getConf();
        user = conf.get("user");
        password = conf.get("password");
        url = conf.get("url");
        driver = conf.get("driver");
        Class.forName(driver);
        scheduledCheck();
    }

    public NewCacheDBPool(String driver, String url, String user, String password)
            throws IOException, ClassNotFoundException {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        Class.forName(this.driver);
        scheduledCheck();
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (connections.size() <= 0) {
            return DriverManager.getConnection(url, user, password);
        } else {
            return connections.poll();
        }
    }

    @Override
    public synchronized void closeConnection(Connection con) throws SQLException {
        /**
         * 关闭数据库连接的时候完成连接池的维护
         * */
        if (connections.size() >= connectionPoolSize) {
            con.close();
        } else {
            connections.add(con);
        }
    }

    /**
     * 设置连接池的大小
     *
     * @param size
     * 线程池的大小
     * @return
     * 返回新建立的线程池
     */
    public NewCacheDBPool setConnectionPoolSize(int size) {
        this.connectionPoolSize = size;
        return this;
    }

    @Override
    public void initSource() {

    }

    @Override
    public void closePool() throws SQLException {
        /**
         * 停止多线程
         * */
        this.service.shutdownNow();
        while (!this.connections.isEmpty()) {
            // 逐个关闭连接
            this.connections.poll().close();
        }
    }

    public NewCacheDBPool setCheckTimeout(int checkTimeout) {
        this.checkTimeout = checkTimeout;
        return this;
    }

    public NewCacheDBPool setCheckScheduleTime(int checkScheduleTime) {
        this.checkScheduleTime = checkScheduleTime;
        return this;
    }

    private void scheduledCheck() {
        /**
         * 线程池支持定时以及周期性执行任务，创建一个corePoolSize为传入参数，最大线程数为整形的最大数的线程池
         *
         *     public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
         *         return new ScheduledThreadPoolExecutor(corePoolSize);
         *     }
         * */
        this.service = Executors.newScheduledThreadPool(1);
        // 定时释放不可用连接
        /**
         * public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
         *                           long initialDelay,
         *                           long period,
         *                           TimeUnit unit);
         * command：执行的任务 Callable或Runnable接口实现类
         * initialDelay：第一次执行任务延迟时间
         * period：连续执行任务之间的周期，从上一个任务开始执行时计算延迟多少开始执行下一个任务，但是还会等上一个任务结束之后。
         * unit：initialDelay和period时间单位
         * */
        this.service.scheduleAtFixedRate(() -> {
            for (Connection each : this.connections) {
                try {
                    if (!each.isValid(this.checkTimeout)) {
                        //false
                        this.connections.remove(each);
                        each.close();
                    }
                } catch (Exception e) {
                    Logger.getLogger("logger").warning(e.toString());
                }
            }
        }, this.checkScheduleTime, this.checkScheduleTime, TimeUnit.SECONDS);
    }
}
