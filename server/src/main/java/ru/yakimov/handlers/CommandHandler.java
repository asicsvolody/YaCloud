/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.mySql.FilesDB;
import ru.yakimov.utils.YaCloudUtils;

import java.sql.SQLException;


public class CommandHandler extends ChannelInboundHandlerAdapter {


    String login;

    Object[] dataObjArr;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        dataObjArr = ((Object[]) msg);

        Commands command;

        if(((ProtocolDataType) dataObjArr[0]).equals(ProtocolDataType.FILE)){
            ctx.fireChannelRead(dataObjArr);
            return;
        }

        command = Commands.getCommand(new String(((byte[]) dataObjArr[2])));
        String commandData = new String(((byte[]) dataObjArr[4]));

        if(command == null)
            return;

        switch (command){
            case SAVE_LOGIN:
                login = commandData;
                return;
            case REFRESH:
            case GO_TO_DIR:
                sendUnits(commandData);
                break;
            case NEW_FOLDER:
                addDir(commandData);
                break;
            case DELETE:
                delete(commandData);
                break;
            case DOWNLOAD_FILE:
                sendFile(commandData);
                break;
        }

        ctx.write(dataObjArr);
    }

    private void delete(String commandData) throws SQLException {
        String [] dataArr = commandData.split(InProtocolHandler.DATA_DELIMITER,3);

        if(dataArr.length <2)
            return;

        String parentDir = dataArr[0].trim();
        String fileName = dataArr[1].trim();
        String fileExt = (dataArr.length == 3)? dataArr[2]:" ";


        if(FilesDB.getInstance().deleteUnit(login, fileName, fileExt, parentDir)){
            sendUnits(parentDir);
        }else{
            writeError(String.format("File %s.%s not found in directory %s",fileName,fileExt,parentDir));
        }
    }

    public void writeError(String msg){
        YaCloudUtils.writeToArrBack(dataObjArr, Commands.ERROR, msg);
    }



    public void sendUnits(String parentDir) {
        try {
            String unitsData = String.join(InProtocolHandler.UNITS_DELIMETER, FilesDB.getInstance().getUnitsFromDir(login, parentDir ));
            YaCloudUtils.writeToArrBack(dataObjArr, Commands.GO_TO_DIR, unitsData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void sendFile(String data) {

    }

    public void addDir(String data) throws SQLException {
        String [] dataArr = data.split(InProtocolHandler.DATA_DELIMITER,2);
        if(dataArr.length!=2)
            return;
        String parentDir = dataArr[0].trim();
        String newFolderName = dataArr[1].trim();
        if(addUnit(newFolderName, " ", parentDir, false, parentDir+newFolderName+"/", 0L)){
            sendUnits(parentDir);
        }else{
            writeError("WRONG NEW FOLDER NAME");
        }
    }

    public boolean addUnit(String name, String ext, String parentDir, boolean isFile, String path, long size) throws SQLException {
        return FilesDB.getInstance().addUnit(
                login
                , name
                , ext
                , parentDir
                , isFile
                , path
                , size);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
