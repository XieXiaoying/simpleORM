package com.xiexy.orm.define;

public class RowValAndTable {
    private RowVal rowVal;
    private String tableName;
    public RowValAndTable(RowVal rowValue, String tableName) {
        super();
        this.rowVal = rowValue;
        this.tableName = tableName;
    }
    public RowVal getRowValue() {
        return rowVal;
    }
    public void setRowValue(RowVal rowValue) {
        this.rowVal = rowVal;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    @Override
    public String toString() {
        return "RowValAndTable [rowVal=" + rowVal + ", tableName=" + tableName + "]";
    }
}
