package com.xiexy.orm.dbPool;

import com.xiexy.orm.tools.ConfSetting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class NewSingleDBPool implements DBPool {
    private String user;
    private String password;
    private String url;
    private String driver;

    public NewSingleDBPool() throws IOException, ClassNotFoundException{
        this("./conf/db.conf");
    }

    public NewSingleDBPool(String file) throws IOException , ClassNotFoundException {
        // 获取配置文件信息
        Map<String, String> conf = ConfSetting.create(file).getConf();
        user = conf.get("user");
        password = conf.get("password");
        url = conf.get("url");
        driver = conf.get("driver");
        Class.forName(driver);
    }

    public NewSingleDBPool(String driver, String url, String user, String password) throws IOException , ClassNotFoundException {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        Class.forName(this.driver);
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        // 获取连接
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public synchronized void closeConnection(Connection con) throws SQLException {
        // 关闭连接
        con.close();
    }

    @Override
    public void initSource() {

    }

    @Override
    public void closePool() throws SQLException {

    }
}
