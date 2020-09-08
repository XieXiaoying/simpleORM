package com.xiexy.orm.dbPool;

import com.xiexy.orm.tools.ConfSetting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class NewFixedDBPool implements DBPool {
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

    public NewFixedDBPool() throws IOException, ClassNotFoundException {
        this("./conf/db.conf");
    }

    public NewFixedDBPool(String file) throws IOException, ClassNotFoundException {

        Map<String, String> conf = ConfSetting.create(file).getConf();
        user = conf.get("user");
        password = conf.get("password");
        url = conf.get("url");
        driver = conf.get("driver");
        Class.forName(driver);
        initSource();
        scheduledCheck();
    }

    public NewFixedDBPool(String driver, String url, String user, String password)
            throws IOException, ClassNotFoundException {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        Class.forName(this.driver);
        initSource();
        scheduledCheck();
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        /**
         *
         * 当线程执行wait()方法时候，会释放当前的锁，然后让出CPU，进入等待状态。
         * */
        while (connections.size() <= 0) {
            // return
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return connections.poll();

    }

    @Override
    public synchronized void closeConnection(Connection con) throws SQLException {
        /**
         * wait()、notify/notifyAll() 在synchronized 代码块执行，说明当前线程一定是获取了锁的
         * 当线程执行wait()方法时候，会释放当前的锁，然后让出CPU，进入等待状态。
         * 当 notify/notifyAll() 被执行时候，才会唤醒一个或多个正处于等待状态的线程，然后继续往下执行
         * 直到执行完synchronized 代码块的代码或是中途遇到wait() ，再次释放锁。
         * 为了保证固定连接数的大小，不随意释放连接，将连接添加到连接池中。
         * */
        connections.add(con);
        notify();
    }

    /**
     * 设置连接池的大小
     *
     * @param size
     * 线程池的大小
     * @return
     * 返回线程池
     */
    public NewFixedDBPool setConnectionPoolSize(int size) {
        this.connectionPoolSize = size;
        return this;
    }

    @Override
    public void initSource() {
        for (int i = 0; i < this.connectionPoolSize; i++) {
            try {
                this.connections.add(DriverManager.getConnection(url, user, password));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closePool() throws SQLException {
        this.service.shutdownNow();
        while (!this.connections.isEmpty()) {
            this.connections.poll().close();
        }
    }

    public NewFixedDBPool setCheckTimeout(int checkTimeout) {
        this.checkTimeout = checkTimeout;
        return this;
    }

    public NewFixedDBPool setCheckScheduleTime(int checkScheduleTime) {
        this.checkScheduleTime = checkScheduleTime;
        return this;
    }
    /**
     * 周期性执行任务
     * 周期性的检查连接是否活着，如果不活着了，重新建立连接
     * 只在定时任务里维护连接，不在其他地方关闭和创建连接
     */
    private void scheduledCheck() {
        this.service = Executors.newScheduledThreadPool(1);
        /**
         * scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
         *
         * command - the task to execute
         * initialDelay - the time to delay first execution
         * period - the period between successive executions
         * unit - the time unit of the initialDelay and period parameters
         *
         * */
        this.service.scheduleAtFixedRate(() -> {
            for (Connection each : this.connections) {
                try {
                    // 如果连接尚未关闭并且仍然有效，则返回 true
                    if (!each.isValid(this.checkTimeout)) {
                        //false
                        this.connections.remove(each);
                        each.close();
                        this.connections.add(DriverManager.getConnection(url, user, password));
                    }
                } catch (Exception e) {
                    Logger.getLogger("logger").warning(e.toString());
                }
            }
        }, this.checkScheduleTime, this.checkScheduleTime, TimeUnit.SECONDS);
    }
}
