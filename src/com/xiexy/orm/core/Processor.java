package com.xiexy.orm.core;

import com.xiexy.orm.annotation.Table;
import com.xiexy.orm.define.RowVal;
import com.xiexy.orm.define.RowValAndTable;
import com.xiexy.orm.exception.ClassNotFoundAnnotation;

import javax.sql.RowSet;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Processor {
    /**
     * 对象转换为表
     * @param object
     * @return
     */
    public static RowValAndTable TableToRV(Object object) throws ClassNotFoundAnnotation {
        Class c = object.getClass();
        // Table注解在c这个类上，就返回true，否则，返回false
        if (c.isAnnotationPresent(Table.class)) {
            Table tname = (Table) c.getAnnotation(Table.class);
            RowVal rValue = RowVal.create();
            /**
             * 反射中获得类的字段的方法
             * getFields()：获得某个类的所有的公共（public）的字段，包括父类中的字段。
             * getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
             * */
            for (Field field : c.getDeclaredFields()) {
                try {
                    // 类外面访问类的私有变量成员时，先需要设置accessible，才可以访问
                    field.setAccessible(true);
                    // 如果object对象的field属性有值，则添加一条，把对象变为rValue list
                    if (field.get(object) != null)
                        rValue.set(field.getName(), String.valueOf(field.get(object)));
                } catch (IllegalArgumentException e) {
                    Logger.getLogger("logger").severe(e.toString());
                } catch (IllegalAccessException e) {
                    Logger.getLogger("logger").severe(e.toString());
                }
            }
            // tname.TableName()获取注解的tablename属性
            return new RowValAndTable(rValue, tname.TableName());
        } else {
            throw new ClassNotFoundAnnotation();
        }
    }

    public static List<Object> RowSetToObject(RowSet rowset, Class c) {
        List<Object> ret = new ArrayList<>();
        try {
            while (rowset.next()) {
                Object object = c.getDeclaredConstructor().newInstance();
                for (Field field : c.getDeclaredFields()) {
                    field.setAccessible(true);
                    // 如果rowset包含这个属性
                    if (rowset.getObject(field.getName()) != null) {
                        if (field.getType().equals(Boolean.TYPE) || field.getType().equals(Boolean.class)) {
                            // boolean
                            field.set(object, Boolean.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Integer.TYPE) || field.getType().equals(Integer.class)) {
                            // int
                            field.set(object, Integer.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Byte.TYPE) || field.getType().equals(Byte.class)) {
                            // byte
                            field.set(object, Byte.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Short.TYPE) || field.getType().equals(Short.class)) {
                            // short
                            field.set(object, Short.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Long.TYPE) || field.getType().equals(Long.class)) {
                            // long
                            field.set(object, Long.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Float.TYPE) || field.getType().equals(Float.class)) {
                            // float
                            field.set(object, Float.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else if (field.getType().equals(Double.TYPE) || field.getType().equals(Double.class)) {
                            // double
                            field.set(object, Double.valueOf(String.valueOf(rowset.getObject(field.getName()))));
                        } else {
                            field.set(object, String.valueOf(rowset.getObject(field.getName())));
                        }
                    } //if (rowset.getObject(field.getName()) != null)
                }
                ret.add(object);
            }
        } catch (Exception e) {
            Logger.getLogger("logger").severe(e.toString());
        }
        return ret;
    }
}
