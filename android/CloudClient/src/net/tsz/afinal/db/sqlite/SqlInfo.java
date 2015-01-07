package net.tsz.afinal.db.sqlite;

import java.util.LinkedList;

public class SqlInfo {
    
    private String tableName;
    private String sql;
    private int action;
    private LinkedList<Object> bindArgs;
    
    public String getSql() {
        return sql;
    }
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public LinkedList<Object> getBindArgs() {
        return bindArgs;
    }
    public void setBindArgs(LinkedList<Object> bindArgs) {
        this.bindArgs = bindArgs;
    }
    
    public Object[] getBindArgsAsArray() {
        if(bindArgs!=null)
            return bindArgs.toArray();
        return null;
    }
    
    public String[] getBindArgsAsStringArray() {
        if(bindArgs!=null){
            String[] strings = new String[bindArgs.size()];
            for(int i = 0;i<bindArgs.size();i++){
                strings[i]=bindArgs.get(i).toString();
            }
            return strings;
        }
        return null;
    }
    
    public void addValue(Object obj){
        if(bindArgs == null)
            bindArgs = new LinkedList<Object>();
        
        bindArgs.add(obj);
    }
    
    public int getAction() {
        return action;
    }
    
    public void setAction(int action) {
        this.action = action;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
