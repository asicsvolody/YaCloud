/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import io.netty.buffer.PoolSubpageMetric;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.utils.MyPackage;

public class VerificationHandler extends ChannelInboundHandlerAdapter {

    private Controller controller;
    private VerificationController verificationController;
    private RegController regController;

    public VerificationHandler() {
        this.controller = SceneAssets.getInstance().getController();
        this.verificationController = SceneAssets.getInstance().getVerificationController();
        this.regController = SceneAssets.getInstance().getRegController();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyPackage myPackage = ((MyPackage) msg);


        if(!verificationController.isAuthorisation()){
            String command = new String(myPackage.getCommandArr());
            if(command.startsWith("authOk")){
                verificationController.setAuthorisation(true,  new String(myPackage.getDataArrForRead()));
                ctx.pipeline().remove(VerificationHandler.this.getClass());
            }
            if(command.startsWith("authError")){
                verificationController.setAuthorisation(false,  new String(myPackage.getDataArrForRead()));
            }
            if(command.startsWith("regOk")){
                regController.showRegScene();
            }
            if(command.startsWith("regError")){
                regController.setRegMsg(new String(myPackage.getDataArrForRead()));
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
