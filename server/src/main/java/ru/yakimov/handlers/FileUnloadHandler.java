/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.IndexProtocol;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Arrays;

public class FileUnloadHandler extends ChannelInboundHandlerAdapter {

    private final String tmpDir ="./tmp/";
//    private ByteBuf accumulator;
    BufferedOutputStream out;
    String parentDir;
    Path file;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

//        ByteBufAllocator allocator = ctx.alloc();
//        accumulator = allocator.directBuffer(1024 * 1024 * 1, 5 * 1024 * 1024);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, SQLException {
        Object[] objArr = (Object[]) msg;
        Commands command = Commands.getCommand(((byte[]) objArr[IndexProtocol.COMMAND.getInt()]));

        System.err.println(command.getString());

        switch (command){
            case START_FILE:


                String[] fileArr = new String(((byte[]) objArr[IndexProtocol.DATA.getInt()]))
                        .split(InProtocolHandler.DATA_DELIMITER,2);

                System.out.println(Arrays.asList(fileArr));

                file = Paths.get(tmpDir+fileArr[0]);
                if(!Files.exists(file.getParent())){
                    Files.createDirectories(file.getParent());
                }
                Files.createFile(file);

                System.out.println(file.getFileName());

                parentDir = fileArr[1];
                out = new BufferedOutputStream(new FileOutputStream(file.toFile(), true));
                break;

            case FILE:

                byte[] bytes = (byte[]) objArr[IndexProtocol.DATA.getInt()];

                System.out.println(bytes.length);

                out.write(bytes);
//                accumulator.writeBytes(bytes);
//
//                while(accumulator.readableBytes() > 0){
//                    out.write(accumulator.readableBytes());
//                }
//
//                accumulator.clear();
                break;

            case END_FILE:
                out.close();
                long fileLength = Longs.fromByteArray(((byte[]) objArr[IndexProtocol.DATA.getInt()]));

                System.out.println(fileLength);

                long realLength = file.toFile().length();
                if(realLength != fileLength){
                    ctx.pipeline()
                            .get(CommandHandler.class)
                            .writeError(String.format("Error unloading file get %s send %s", realLength, fileLength));
                    return;
                }

                Files.createDirectories(Paths.get("./"+parentDir));

                Files.move(file
                        , Paths.get("./"+parentDir+file.getFileName())
                        , StandardCopyOption.REPLACE_EXISTING );
                String[] fileNameArr = file.getFileName().toString().split("\\.",2);
                if(ctx.pipeline().get(CommandHandler.class)
                        .addUnit(fileNameArr[0], fileNameArr[1], parentDir, true, parentDir+file.getFileName(), fileLength)){
                    ctx.pipeline().get(CommandHandler.class).sendUnits(parentDir);
                    out = null;
                    file = null;
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
