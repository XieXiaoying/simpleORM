package com.xiexy.orm.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ConfSetting {
    private String filename;
    private Map<String, String> conf = new HashMap<>();

    public ConfSetting(String name) {
        this.filename = name;
    }
    // 静态创建对象
    public static ConfSetting create(String filename) {
        return new ConfSetting(filename);
    }
    private void initReader(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                parse(line, conf); // 添加
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parse(String line, Map<String, String> conf) {
        int i = line.indexOf('#');
        // 读注释之前的信息
        if (i >= 0)
            line = line.substring(0, i);
        // 去掉空格
        line = line.trim();
        // 如果是。。。。。=。。。。。的形式，则把这一行配置信息，转化为key value的形式存储
        if (line.matches(".+=.+")) {
            i = line.indexOf('=');
            String name = line.substring(0, i);
            String value = line.substring(i + 1);
            if (!name.trim().equals("") && !value.trim().equals(""))
                conf.put(name.trim(), value.trim());
        }
    }
}
