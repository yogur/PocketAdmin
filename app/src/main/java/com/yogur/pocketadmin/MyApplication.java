package com.yogur.pocketadmin;

import android.app.Application;

/**
 * Created by abed on 12/23/15.
 */
public class MyApplication extends Application {

    private create_connection c;
    private String dbName = "";
    private String hostName = "";

    public void setConn(create_connection conn){
        this.c = conn;
    }

    public create_connection getConn(){

        return c;
    }

    public void setDbName(String dbName){
        this.dbName = dbName;
    }

    public String getDbName(){
        return this.dbName;
    }

    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public String getHostName(){
        return this.hostName;
    }

}
