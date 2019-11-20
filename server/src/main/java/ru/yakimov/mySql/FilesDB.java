/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.mySql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilesDB {
    private Connection connection;

    private static FilesDB instance;

    public static FilesDB getInstance() throws SQLException {
        FilesDB localInstance = instance;
        if(localInstance == null){
            synchronized (FilesDB.class){
                localInstance = instance;
                if(localInstance == null){
                    localInstance = instance = new FilesDB();
                }
            }
        }
        return  localInstance;
    }

    private FilesDB() throws SQLException {
        this.connection = MySqlDb.getInstance().getConnection();
    }

    public void createUserTable(String login) throws SQLException {
        try(Statement stmt = connection.createStatement();) {
            String sql = String.format("CREATE TABLE `yaCloudDB`.`%s` (" +
                    "  `unit_name` varchar(45) NOT NULL," +
                    "  `unit_ext` varchar(10) NOT NULL," +
                    "  `unit_parent` varchar(500) NOT NULL," +
                    "  `unit_is_file` tinyint(4) NOT NULL," +
                    "  `unit_path` varchar(1000) NOT NULL," +
                    "  `unit_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  `unit_size` int(11) NOT NULL DEFAULT 0," +
                    "  PRIMARY KEY (`unit_name`,`unit_parent`,`unit_is_file`,`unit_ext`)," +
                    "  UNIQUE KEY `unit_path_UNIQUE` (`unit_path`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8",login);

            System.out.println(sql);
            stmt.execute(sql);
        }
    }

    public boolean isFile(String login, String name, String dir, String ext){
        int isFile = 0;
        try(Statement stmt = connection.createStatement()){
            String sql = String.format("SELECT unit_is_file FROM yaCloudDB.%s WHERE unit_parent='%s' AND unit_name='%s' AND unit_ext='%s'", login, dir, name, ext);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                isFile = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isFile == 1;
    }

    public List<String> getUnitsFromDir(String login, String dir) {

        try(Statement stmt = connection.createStatement()){
            String sql = String.format("SELECT unit_is_file,unit_name,unit_ext,unit_size,unit_date FROM yaCloudDB.%s WHERE unit_parent='%s'", login, dir);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            List<String> resArr = new ArrayList<>();
            resArr.add(dir);

            while(rs.next()){
                resArr.add(new StringBuilder(rs.getString(1)).append(" ")
                        .append(rs.getString(2)).append(" ")
                        .append(rs.getString(3)).append(" ")
                        .append(rs.getString(4)).append(" ")
                        .append(rs.getString(5)).toString()
                );
            }
            rs.close();

            return resArr;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUnit(String login, String unitName,String ext, String unitParent) throws SQLException {
//        if(isDirectory(login,unitName,unitParent,ext)){
//            deleteContent(login,unitName,unitParent);
//        }
        String sql = String.format("DELETE FROM yaCloudDB.%s WHERE unit_name=? AND unit_parent=? AND unit_ext=?",login);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, unitName);
            ps.setString(2, unitParent);
            ps.setString(3, ext);
            System.out.println(ps);

            System.out.println(ps.toString());
            return ps.executeUpdate() == 1;
        }
    }

    private boolean isDirectory(String login, String unitName, String unitParent, String ext) {
        return !isFile(login,unitName,unitParent,ext);
    }

    public boolean addUnit(String login, String unitName, String unitExt, String unitParent, Boolean isFile, String unitPath, Long unitSize) throws SQLException {

        String sql = String.format("INSERT INTO yaCloudDB.%s (unit_name,unit_ext,unit_parent,unit_is_file, unit_path, unit_size) VALUES (?,?,?,?,?,?)",login);
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, unitName);
            ps.setString(2, unitExt);
            ps.setString(3, unitParent);
            ps.setBoolean(4, isFile);
            ps.setString(5, unitPath);
            ps.setLong(6, unitSize);
            System.out.println(ps.toString());
            return ps.executeUpdate() == 1;
        }
    }
}
