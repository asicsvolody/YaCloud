package ru.yakimov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

public class CloudHandler extends ChannelInboundHandlerAdapter {

    public static final String DOWNLOAD_DIR = "./downloads/";



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Object[] dataObj = ((Object[]) msg);

        ProtocolDataType type = ((ProtocolDataType) dataObj[IndexProtocol.TYPE.getInt()]);


        if(type.equals(ProtocolDataType.FILE)){
            System.err.println("Get file annotation");
            ctx.fireChannelRead(dataObj);
            return;

        }

        Commands command = Commands.getCommand(new String((byte[]) dataObj[2]));
        if(command == null)
            return;

        String dataMsg = new String((byte[]) dataObj[IndexProtocol.DATA.getInt()]);
        switch (command){
            case GO_TO_DIR:
                SceneAssets.getInstance().getController().initializeUnitListView(dataMsg.split("//%//"));
                break;
            case ERROR:
                SceneAssets.getInstance().getController().showAlertError("ERROR", dataMsg);
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
