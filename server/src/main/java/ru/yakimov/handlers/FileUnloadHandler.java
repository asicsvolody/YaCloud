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
import ru.yakimov.utils.MyPackage;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;

public class FileUnloadHandler extends ChannelInboundHandlerAdapter {

    private final String tmpDir ="./tmp/";
    private BufferedOutputStream out;
    private String parentDir;
    private Path file;
    int packNumber = 0;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, SQLException {

        MyPackage myPackage = ((MyPackage) msg);
        Commands command = Commands.getCommand(myPackage.getCommandArr());

        System.err.println(command.getString());


        switch (command){
            case START_FILE:
                packNumber = 0;
                String[] fileArr = new String(myPackage.getDataArrForRead())
                        .split(InProtocolHandler.DATA_DELIMITER,2);


                file = Paths.get(tmpDir+fileArr[0]);
                if(!Files.exists(file.getParent())){
                    Files.createDirectories(file.getParent());
                }
                Files.deleteIfExists(file);

                Files.createFile(file);

                System.out.println(file.getFileName());

                parentDir = fileArr[1];
                out = new BufferedOutputStream(new FileOutputStream(file.toFile(), true));
                myPackage.disable();
                break;

            case FILE:
                packNumber++;
                System.err.println("pack# "+ packNumber);
//                byte[] bytes = (byte[]) objArr[IndexProtocol.DATA.getInt()];
//                System.out.println(bytes.length);
                out.write(myPackage.getDataArrForRead());
                myPackage.disable();
                break;

            case END_FILE:
                out.close();
                long fileLength = Longs.fromByteArray(myPackage.getDataArrForRead());
                long realLength = file.toFile().length();
                if(realLength != fileLength){
                    System.err.println("Dif SIZe "+realLength +" AND "+fileLength);
                    ctx.pipeline()
                            .get(CommandHandler.class)
                            .writeError(String.format("Error unloading file get %s send %s", realLength, fileLength));
                    return;
                }
                String userDir = ctx.pipeline().get(CommandHandler.class).getUserDir();
                Files.createDirectories(Paths.get(userDir+parentDir));
                Files.move(file
                        , Paths.get(userDir+parentDir+file.getFileName())
                        , StandardCopyOption.REPLACE_EXISTING );
                String[] fileNameArr = file.getFileName().toString().split("\\.",2);
                if(ctx.pipeline().get(CommandHandler.class)
                        .addUnit(fileNameArr[0], fileNameArr[1], parentDir, true, parentDir+file.getFileName(), fileLength)){
                    ctx.pipeline().get(CommandHandler.class).sendUnits(parentDir, ctx);
                    out = null;
                    file = null;
                    parentDir = null;
                }
        }

        System.out.println("########################################");
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(out != null)
            out.close();
        ctx.close();
    }
}
