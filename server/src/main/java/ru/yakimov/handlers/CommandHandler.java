/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import com.google.common.primitives.Longs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.IndexProtocol;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.mySql.FilesDB;
import ru.yakimov.utils.YaCloudUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;


public class CommandHandler extends ChannelInboundHandlerAdapter {


    String login;
    String userDir;

    Object[] dataObjArr;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        dataObjArr = ((Object[]) msg);

        Commands command;
        
        ProtocolDataType type = ((ProtocolDataType) dataObjArr[IndexProtocol.TYPE.getInt()]);

        System.out.println(type.getFirstMessageByte());

        if(type.equals(ProtocolDataType.FILE)){
            System.err.println("Get file annotation");
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
                userDir = "./STORAGE/"+login+"/";
                return;
            case DOWNLOAD_FILE:
                sendFile(commandData, ctx);
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

        }

        ctx.write(dataObjArr);
    }

    private void delete(String commandData) throws SQLException, IOException {
        String [] dataArr = commandData.split(InProtocolHandler.DATA_DELIMITER,3);

        if(dataArr.length <2)
            return;

        String parentDir = dataArr[0].trim();
        String fileName = dataArr[1].trim();
        String fileExt = (dataArr.length == 3)? dataArr[2]:" ";

        Path unitPath = null;

        if(FilesDB.getInstance().isFile(login,fileName,parentDir,fileExt)){
            unitPath = Paths.get(new StringBuilder("./").append(parentDir).append(fileName).append(".").append(fileExt).toString());

        }else{
            unitPath = Paths.get(new StringBuilder("./").append(parentDir).append(fileName).append("/").toString());
        }
            deleteAll(unitPath);

        if(FilesDB.getInstance().deleteUnit(login, fileName, fileExt, parentDir)){
            sendUnits(parentDir);
        }else{
            writeError(String.format("File %s.%s not found in directory %s",fileName,fileExt,parentDir));
        }
    }

    public void writeError(String msg){
        YaCloudUtils.writeToArrBackCommand(dataObjArr, Commands.ERROR, msg);
    }



    public void sendUnits(String parentDir) {
        try {
            String unitsData = String.join(InProtocolHandler.UNITS_SEPARATOR, FilesDB.getInstance().getUnitsFromDir(login, parentDir ));
            YaCloudUtils.writeToArrBackCommand(dataObjArr, Commands.GO_TO_DIR, unitsData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private void deleteAll(Path path)  {
        try {
            if(Files.isDirectory(path)) {
                    Files.list(path).forEach(this::deleteAll);
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUnits(String parentDir, ChannelHandlerContext ctx) {
       sendUnits(parentDir);
        ctx.write(dataObjArr);
    }

    public void sendFile(String data, ChannelHandlerContext ctx) {
        String[] dataArr = data.split(InProtocolHandler.DATA_DELIMITER,3);
        String fileName = dataArr[1]+"."+dataArr[2];
        String parent = dataArr[0];
        Path file = Paths.get("./"+parent+fileName);
        if(Files.notExists(file)){
            writeError("FIle not exist:" + file);
            ctx.write(dataObjArr);
            return;
        }

        YaCloudUtils.writeToArrBackFile(dataObjArr, Commands.START_FILE, fileName);
        ctx.write(dataObjArr);

        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))){
            byte[] byteArray = new byte[20480];
            int i = -1;
            while ((i = in.read(byteArray)) != -1){

                YaCloudUtils.writeToArrBackFile(dataObjArr, Commands.FILE, Arrays.copyOf(byteArray, i));
                ctx.write(dataObjArr);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        YaCloudUtils.writeToArrBackFile(dataObjArr, Commands.END_FILE, Longs.toByteArray(file.toFile().length()));
        ctx.write(dataObjArr);

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

    public String getUserDir() {
        return userDir;
    }
}
