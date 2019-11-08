/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.mySql;

import java.sql.*;

public class MySqlDb {

    private final String JDBC = "com.mysql.cj.jdbc.Driver";

    private Connection conn;

    private String dbUrl = "jdbc:mysql://localhost:3306/YaCloudDB?serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL" ;

    private String dbUser = "vladimir";

    private String dbPass = "bhbyf.hnftdf";



    private static MySqlDb instance;

    public static MySqlDb getInstance(){
        MySqlDb localInstance = instance;
        if(localInstance == null){
            synchronized (MySqlDb.class){
                localInstance = instance;
                if(localInstance == null){
                    localInstance = instance = new MySqlDb();
                }
            }
        }
        return  localInstance;
    }




    public boolean isDbConnected() {
        final String CHECK_SQL_QUERY = "SELECT 1";
        boolean isConnected = false;
        try {
            conn.prepareStatement(CHECK_SQL_QUERY).execute();
            isConnected = true;
        }
        catch (SQLException | NullPointerException e) {
           e.printStackTrace();
        }
        return isConnected;
    }

    private MySqlDb() {
    }

    public Connection initConnection(String host, String port, String schema, String user, String pass) throws SQLException {
        dbUrl = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL",host,port, schema);
        dbUser = user;
        dbPass = pass;
        return getConnection();
    }

    public Connection getConnection() throws SQLException {

        if (conn == null || (conn != null && !isDbConnected())) {
            try {
                Class.forName(JDBC);
            }catch (ClassNotFoundException e) {
                System.out.println("Error to connection to MySql");
                throw new SQLException("Error to connection to MySql!");
            }
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            conn.setAutoCommit(false);
            return conn;
        }
        return conn;
    }

    public void closeConnection() {
        if (conn == null) {
            return;
        }
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
