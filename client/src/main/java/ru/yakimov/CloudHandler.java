package ru.yakimov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.utils.MyPackage;

/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

public class CloudHandler extends ChannelInboundHandlerAdapter {

    public static final String DOWNLOAD_DIR = "./downloads/";



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        MyPackage myPackage = ((MyPackage) msg);

        ProtocolDataType type = myPackage.getType();


        if(type.equals(ProtocolDataType.FILE)){
            System.err.println("Get file annotation");
            ctx.fireChannelRead(myPackage);
            return;

        }

        Commands command = Commands.getCommand(new String(myPackage.getCommandArr()));
        if(command == null){
            myPackage.disable();
            return;
        }

        String dataMsg = new String(myPackage.getDataArrForRead());
        switch (command){
            case GO_TO_DIR:
                SceneAssets.getInstance().getController().initializeUnitListView(dataMsg.split("//%//"));
                break;
            case ERROR:
                SceneAssets.getInstance().getController().showAlertError("ERROR", dataMsg);
        }

        myPackage.disable();


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
