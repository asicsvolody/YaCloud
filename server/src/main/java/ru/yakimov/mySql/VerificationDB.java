/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.mySql;

import ru.yakimov.MyServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VerificationDB {

    private Connection connection;

    private static VerificationDB instance;

    public static VerificationDB getInstance() throws SQLException {
        VerificationDB localInstance = instance;
        if(localInstance == null){
            synchronized (VerificationDB.class){
                localInstance = instance;
                if(localInstance == null){
                    localInstance = instance = new VerificationDB();
                }
            }
        }
        return  localInstance;
    }


    private VerificationDB() throws SQLException {
        connection = MySqlDb.getInstance().getConnection();
    }

    public boolean isUser(String login, String pass) throws SQLException {
        PreparedStatement ps = connection
                .prepareStatement("SELECT * FROM yaCloudDB.users WHERE users_login =? and users_pass=?");
        ps.setString(1,login);
        ps.setString(2, pass);
        ResultSet rs = ps.executeQuery();
        boolean result = rs.next();
        rs.close();
        ps.close();
        return result;
    }

    public boolean isUser(String login) throws SQLException {
        PreparedStatement ps = connection
                .prepareStatement("SELECT * FROM yaCloudDB.users WHERE users_login =?");
        ps.setString(1,login);
        ResultSet rs = ps.executeQuery();
        boolean result =  rs.wasNull();
        rs.close();
        ps.close();
        return result;
    }

    public boolean registration(String login, String pass, String eMail, String controlWord) throws SQLException {
        if(isUser(login))
            return false;

        PreparedStatement ps = connection.prepareStatement("INSERT INTO yaCloudDB.users (users_login, users_pass, users_eMail, users_controlWord) VALUES (?,?,?,?);");
        ps.setString(1,login);
        ps.setString(2,pass);
        ps.setString(3,eMail);
        ps.setString(4,controlWord);
        boolean res = ps.execute();

        if(res){
            res = FilesDB.getInstance().createUserTable(login);
        }
        if(!res)
            deleteUser(login);
        ps.close();
        return res;
    }


    private boolean deleteUser(String login) throws SQLException {
        try(PreparedStatement ps = connection
                .prepareStatement("DELETE * FROM yaCloudDB.users where user_login =?")){
            ps.setString(1, login);
            return ps.execute();
        }
    }
}
