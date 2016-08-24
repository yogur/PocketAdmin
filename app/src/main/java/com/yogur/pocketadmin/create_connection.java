package com.yogur.pocketadmin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by abed on 12/15/15.
 */
public class create_connection {

    private String user;
    private String passwd;
    private String serverName;
    private String dbName;
    private int portNumber;

    public create_connection(){}

    public create_connection(String usr,String pwd,String serv_nm,String db_nm,int port){

        this.user = usr;
        this.passwd = pwd;
        this.serverName = serv_nm;
        this.dbName = db_nm;
        this.portNumber = port;
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {

        //should be used with this version of mysql jdbc driver
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user",this.user);
        connectionProps.put("password",this.passwd);

        conn = DriverManager.getConnection(
                "jdbc:"+ "mysql" + "://" +
                        this.serverName +
                        ":" + this.portNumber + "/"
                        + this.dbName
                        + "?allowMultiQueries=true",
                connectionProps
        );

        //System.out.println("Connected to database");
        return conn;
    }

    public Connection getConnectionNoMultipleQueries() throws SQLException, ClassNotFoundException {

        //should be used with this version of mysql jdbc driver
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user",this.user);
        connectionProps.put("password",this.passwd);

        conn = DriverManager.getConnection(
                "jdbc:"+ "mysql" + "://" +
                        this.serverName +
                        ":" + this.portNumber + "/"
                        + this.dbName
                        + "?allowMultiQueries=false" //MultiQueries False for select statements
                ,connectionProps
        );

        //System.out.println("Connected to database");
        return conn;
    }

    public void setDB (String db){
        this.dbName = db;
    }
    public String getDB(){ return this.dbName;}

    public String getPKColumnName(Connection conn,String tableName) throws SQLException {

        String   catalog   = null;
        String   schema    = null;

        String columnName = null;

        ResultSet result = conn.getMetaData().getPrimaryKeys(
                catalog, schema, tableName);

        while(result.next()){
            columnName = result.getString("COLUMN_NAME");
        }

        return columnName;
    }

    public List<String> getPrimarykeysList(Connection conn,String tableName) throws SQLException {

        String   catalog   = null;
        String   schema    = null;

        List<String> PKs = new ArrayList<>();

        ResultSet result = conn.getMetaData().getPrimaryKeys(
                null, null, tableName);

        while(result.next()){
            PKs.add(result.getString("COLUMN_NAME"));
        }

        return PKs;
    }

    public List<String> getUniqueColumnsList(Connection conn,String tableName) throws SQLException {

        String   catalog   = null;
        String   schema    = null;
        boolean unique = true;
        boolean approximate = true;

        List<String> uniqueColumns = new ArrayList<>();

        ResultSet result = conn.getMetaData().getIndexInfo(catalog,
                schema, tableName, unique, approximate);

        while(result.next()){
            if(result.getBoolean("NON_UNIQUE") == false)
                uniqueColumns.add(result.getString("COLUMN_NAME"));
        }

        return uniqueColumns;
    }

    public String getUniqueColumn(Connection conn,String tableName) throws SQLException {

        String   catalog   = null;
        String   schema    = null;
        boolean unique = true;
        boolean approximate = true;

        String uniqueColumn = null;

        ResultSet result = conn.getMetaData().getIndexInfo(catalog,
                schema, tableName, unique, approximate);

        while(result.next()){
            if(result.getBoolean("NON_UNIQUE") == false) {
                //returns the first occurrence of a unique column, whether it is a primary key or unique
                uniqueColumn = result.getString("COLUMN_NAME");
                return uniqueColumn;
            }
        }

        return uniqueColumn;
    }

    public ResultSet retrieveData(Connection conn,String query) throws SQLException {

        Statement stmt = null;
        //List<String> data = new ArrayList<>();

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        /*
            int columnCount = rs.getMetaData().getColumnCount();

            while(rs.next())
                for (int i = 1; i <= columnCount; i++)
                    data.add(rs.getString(i));
*/

        //if(stmt != null)
        //  stmt.close();

        return rs;
    }

    /*
   public List<String> getColumnNames(Connection conn,String tableName) throws SQLException{

        List<String> colNames = new ArrayList<>();

        ResultSet rs = retrieveData(conn,"SELECT * FROM " + tableName + " LIMIT 1");
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for(int i = 1; i <= columnCount; i++)
            colNames.add(rsmd.getColumnName(i));

        return colNames;

    }*/

    public void test(Connection conn) throws SQLException{

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, "users", null);
        while (rsColumns.next()) {
            String columnName = rsColumns.getString("COLUMN_NAME");
            System.out.println("column name=" + columnName);
            String columnType = rsColumns.getString("TYPE_NAME");
            System.out.println("type:" + columnType);
            int size = rsColumns.getInt("COLUMN_SIZE");
            System.out.println("size:" + size);
            int nullable = rsColumns.getInt("NULLABLE");
            if (nullable == DatabaseMetaData.columnNullable) {
                System.out.println("nullable true");
            } else {
                System.out.println("nullable false");
            }
            int position = rsColumns.getInt("ORDINAL_POSITION");
            System.out.println("position:" + position);

        }
    }

    public List<String> getColumnNames(Connection conn,String tableName) throws SQLException{

        List<String> colNames = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next())
            colNames.add(rsColumns.getString("COLUMN_NAME"));

        return colNames;
    }

    public List<String> getColumnTypes(Connection conn,String tableName) throws SQLException{

        List<String> colTypes = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next())
            colTypes.add(rsColumns.getString("TYPE_NAME"));

        return colTypes;
    }

    public List<Integer> getColumnSizes(Connection conn,String tableName) throws SQLException{

        List<Integer> colSizes = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next())
            colSizes.add(rsColumns.getInt("COLUMN_SIZE"));

        return colSizes;
    }

    public List<String> getColumnTypeAndSize(Connection conn, String tableName) throws SQLException{

        List<String> colTypeSize = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next()) {
            String temp = rsColumns.getString("TYPE_NAME");
            int size = rsColumns.getInt("COLUMN_SIZE");
            if(size != 0)
                temp += "(" + size + ")";
            colTypeSize.add(temp);
        }

        return colTypeSize;
    }

    public List<String> getColumnDefaultValues(Connection conn,String tableName) throws SQLException{

        List<String> colDefVal = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next()) {
            colDefVal.add(rsColumns.getString("COLUMN_DEF"));
            //System.out.println("t" + rsColumns.getString("COLUMN_DEF"));
            if(rsColumns.wasNull()){
                System.out.println("here");
            }
        }

        return colDefVal;
    }

    public List<String> getColumnIsAutoIncrement(Connection conn,String tableName) throws SQLException{

        List<String> colIsAuto = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next())
            colIsAuto.add(rsColumns.getString("IS_AUTOINCREMENT"));

        return colIsAuto;
    }

    public List<String> getColumnNullable(Connection conn,String tableName) throws SQLException{

        List<String> colNull = new ArrayList<>();

        ResultSet rsColumns = null;
        DatabaseMetaData meta = conn.getMetaData();
        rsColumns = meta.getColumns(null, null, tableName, null);

        while (rsColumns.next())
            if (rsColumns.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                colNull.add("NULL");
            else
                colNull.add("NOT NULL");


        return colNull;
    }

    public List<String> getSingleRow(Connection conn,String tableName,String pkName,String pkValue) throws SQLException{

        List<String> row = new ArrayList<>();

        Statement stmt = null;

        stmt = conn.createStatement();
        String query = "SELECT * FROM " + tableName + " WHERE " + pkName + " = '" + pkValue + "'";
        ResultSet rs = stmt.executeQuery(query);

            int columnCount = rs.getMetaData().getColumnCount();

            while(rs.next())
                for (int i = 1; i <= columnCount; i++)
                    row.add(rs.getString(i));


        if(stmt != null)
            stmt.close();

        return row;
    }

    public void execQuery(Connection conn,String query) throws SQLException {

        Statement stmt = null;

        stmt = conn.createStatement();
        stmt.execute(query);

        if(stmt != null)
            stmt.close();
    }

    public List<String> showDatabases(Connection conn) throws SQLException{

        List<String> dbs = new ArrayList<>();

        try {
            ResultSet rs = conn.getMetaData().getCatalogs();
            int columnCount = rs.getMetaData().getColumnCount();

            while(rs.next()){

                for(int i = 1; i <= columnCount; i++)
                    dbs.add(rs.getString(i));
            }
        } catch (SQLException e){
            System.err.println("Message: " + e.getMessage());
        }

        return dbs;
    }

    public List<String> showTables(Connection conn) throws SQLException{

        List<String> tables = new ArrayList<>();

        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);

            while(rs.next())
                tables.add(rs.getString(3));

        } catch (SQLException e){
            System.err.println("Message: " + e.getMessage());
        }

        return tables;
    }

}

