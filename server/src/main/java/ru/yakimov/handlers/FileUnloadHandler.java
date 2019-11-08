/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.IndexProtocol;
import ru.yakimov.mySql.FilesDB;

import java.io.*;
import java.net.CookieHandler;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.sql.SQLException;

public class FileUnloadHandler extends ChannelInboundHandlerAdapter {

    private final String tmpDir ="./tmp/";
    private ByteBuf accumulator;
    BufferedOutputStream out;
    String fileName;
    String parentDir;
    long fileLength;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBufAllocator allocator = ctx.alloc();
        accumulator = allocator.directBuffer(1024 * 1024 * 1, 5 * 1024 * 1024);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, SQLException {
        Object[] objArr = (Object[]) msg;
        Commands command = Commands.getCommand(((byte[]) objArr[IndexProtocol.COMMAND.getInt()]));

        switch (command){
            case START_FILE:

                fileLength = 0;
                String[] fileArr = new String(((byte[]) objArr[IndexProtocol.DATA.getInt()])).split(InProtocolHandler.DATA_DELIMITER,2);
                fileName = fileArr[0];
                parentDir = fileArr[1];
                out = new BufferedOutputStream(new FileOutputStream(tmpDir + fileName, true));
                break;

            case FILE:

                byte[] bytes = (byte[]) objArr[IndexProtocol.DATA.getInt()];
                fileLength += bytes.length;
                accumulator.writeBytes(bytes);

                while(accumulator.readableBytes() > 0){
                    out.write(accumulator.readableBytes());
                }

                accumulator.clear();
                break;

            case END_FILE:
                out.close();
                long fileLengthClient = new Long(new String((byte[]) objArr[IndexProtocol.DATA.getInt()]));
                if(fileLengthClient != fileLength){
                    ctx.pipeline().get(CommandHandler.class).writeError(String.format("Error unloading file get %s send %s", fileLength, fileLengthClient));
                    return;
                }
                Files.createDirectories(Paths.get("./"+parentDir));
                Files.move(Paths.get(tmpDir+fileName), Paths.get("./"+parentDir+fileName), StandardCopyOption.REPLACE_EXISTING );
                String[] fileNameArr = fileName.split("\\.",2);
                if(ctx.pipeline().get(CommandHandler.class)
                        .addUnit(fileNameArr[0], fileNameArr[1], parentDir, true, parentDir+fileName, fileLength )){
                    ctx.pipeline().get(CommandHandler.class).sendUnits(parentDir);
                    accumulator = null;
                    out = null;
                    fileName = null;
                    parentDir = null;
                }
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(out != null)
            out.close();
        ctx.close();
    }
}
