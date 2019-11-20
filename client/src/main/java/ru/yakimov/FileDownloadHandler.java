/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import com.google.common.primitives.Longs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.utils.MyPackage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloadHandler extends ChannelInboundHandlerAdapter {

    private final String DOWNLOAD_DIR ="./download/";
    private BufferedOutputStream out;
    private String parentDir;
    private Path file;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {

        MyPackage myPackage = ((MyPackage) msg);
//        Object[] objArr = (Object[]) msg;
        Commands command = Commands.getCommand(myPackage.getCommandArr());

        System.err.println(command.getString());

        switch (command) {
            case START_FILE:

                String fileName = new String(myPackage.getDataArrForRead());

                file = Paths.get(DOWNLOAD_DIR + fileName);
                if (!Files.exists(file.getParent())) {
                    Files.createDirectories(file.getParent());
                }
                Files.deleteIfExists(file);

                Files.createFile(file);

                System.out.println(file.getFileName());

                out = new BufferedOutputStream(new FileOutputStream(file.toFile(), true));
                break;

            case FILE:
                System.out.println("Writing file");
                out.write(myPackage.getDataArrForRead());
                break;

            case END_FILE:
                out.close();
                long fileLength = Longs.fromByteArray(myPackage.getDataArrForRead());
                long realLength = file.toFile().length();
                if (realLength != fileLength) {
                    SceneAssets.getInstance().getController().showAlertError("Download error",
                            "File is break");
                }
                out = null;
                file = null;
                parentDir = null;
        }
        myPackage.disable();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if(out != null)
            out.close();
        ctx.close();
    }
}
