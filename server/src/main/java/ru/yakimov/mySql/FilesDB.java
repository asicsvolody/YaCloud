/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.mySql;

import ru.yakimov.MyServer;

import javax.swing.text.html.ListView;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean createUserTable(String login) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(
                "CREATE TABLE ?" +
                        "  `unit_name` VARCHAR(45) NOT NULL," +
                        "  `unit_ext` VARCHAR(10) NOT NULL," +
                        " `unit_parent` VARCHAR(45) NOT NULL," +
                        " `unit_is_file` TINYINT NOT NULL," +
                        " `unit_path` VARCHAR(45) NOT NULL," +
                        " `unit_date` DATETIME NOT NULL DEFAULT now()," +
                        " `unit_size` LONGBLOB NOT NULL," +
                        " PRIMARY KEY (`unit_name`, `unit_ext`, `unit_parent`, `unit_is_file`)," +
                        " UNIQUE INDEX `unit_path_UNIQUE` (`unit_path` ASC));"
        )) {

            ps.setString(1, login);
            return ps.execute();
        }
    }

    public List<String> getUnitsFromDir(String login, String dir) {

        try(Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(String.format("SELECT unit_is_file,unit_name,unit_ext,unit_size,unit_date FROM yaCloudDB.%s WHERE unit_parent='%s'", login, dir));
            List<String> resArr = new ArrayList<>();

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

    public boolean addUnit(String login, String unitName, String unitExt, String unitParent, Boolean isFile, String unitPath, Long unitSize) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO ? (unit_name, unit_ext, unit_parent, unit_is_file, unit_path, unit_size) " +
                        "VALUES (?,?,?,?,?,?);"
        )) {
            ps.setString(1, login);
            ps.setString(2, unitName);
            ps.setString(3, unitExt);
            ps.setString(4, unitParent);
            ps.setBoolean(5, isFile);
            ps.setString(6, unitPath);
            ps.setLong(7, unitSize);
            return ps.execute();
        }
    }
}
