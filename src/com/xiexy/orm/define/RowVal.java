package com.xiexy.orm.define;

import java.util.ArrayList;
import java.util.List;

public class RowVal {
    // 定义一个枚举类，取值为and和or
    public enum Relation{And, Or};
    private List<TextVal> row = new ArrayList<>();
    // 静态方法create用于创建对象
    public static RowVal create() {
        return new RowVal();
    }

    public RowVal set(String k, String v) {
        this.row.add(TextVal.create().setText(k).setValue(v));
        // return this的话，就可以链式调用
        return this;
    }

    public RowVal set(String k, int v) {
        this.row.add(TextVal.create().setText(k).setValue(v));
        return this;
    }

    public RowVal set(String k, long v) {
        this.row.add(TextVal.create().setText(k).setValue(v));
        return this;
    }

    public RowVal set(String k, Double v) {
        this.row.add(TextVal.create().setText(k).setValue(v));
        return this;
    }

    public RowVal set(String k, Boolean v) {
        this.row.add(TextVal.create().setText(k).setValue(v));
        return this;
    }

    public List<TextVal> getRow() {
        return row;
    }

    @Override
    public String toString() {
        String ret = "";
        for (TextVal tValue : row) {
            ret += tValue.filed + "=>" + tValue.value + "\n";
        }
        return "RowVal [row=" + ret + "]";
    }
}
