/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import com.google.common.primitives.Longs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.handlers.InProtocolHandler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Arrays;

public class FileDownloadHandler extends ChannelInboundHandlerAdapter {

    private final String DOWNLOAD_DIR ="./download/";
    private BufferedOutputStream out;
    private String parentDir;
    private Path file;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, SQLException {
        Object[] objArr = (Object[]) msg;
        Commands command = Commands.getCommand(((byte[]) objArr[IndexProtocol.COMMAND.getInt()]));

        System.err.println(command.getString());

        switch (command) {
            case START_FILE:

                String fileName = new String(((byte[]) objArr[IndexProtocol.DATA.getInt()]));

                file = Paths.get(DOWNLOAD_DIR + fileName);
                if (!Files.exists(file.getParent())) {
                    Files.createDirectories(file.getParent());
                }
                Files.createFile(file);

                System.out.println(file.getFileName());

                out = new BufferedOutputStream(new FileOutputStream(file.toFile(), true));
                break;

            case FILE:
                byte[] bytes = (byte[]) objArr[IndexProtocol.DATA.getInt()];
                System.out.println(bytes.length);
                out.write(bytes);
                break;

            case END_FILE:
                out.close();
                long fileLength = Longs.fromByteArray(((byte[]) objArr[IndexProtocol.DATA.getInt()]));
                long realLength = file.toFile().length();
                if (realLength != fileLength) {
                    SceneAssets.getInstance().getController().showAlertError("Download error",
                            "File is break");
                }
                out = null;
                file = null;
                parentDir = null;
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
