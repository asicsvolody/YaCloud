package ru.yakimov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

public class CloudHandler extends ChannelInboundHandlerAdapter {



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object[] msgArr = ((Object[]) msg);
        if(((ProtocolDataType) msgArr[0]).equals(ProtocolDataType.FILE)){
            ctx.fireChannelRead(msg);
        }

        Commands command = Commands.getCommand(new String((byte[]) msgArr[2]));
        if(command == null)
            return;

        String dataMsg = new String((byte[]) msgArr[IndexProtocol.DATA.getInt()]);
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
