/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

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
        Object[] msgArr = ((Object[]) msg);


        if(!verificationController.isAuthorisation()){
            String command = new String(((byte[]) msgArr[2]));
            if(command.startsWith("authOk")){
                verificationController.setAuthorisation(true,  new String(((byte[]) msgArr[4])));
                ctx.pipeline().remove(VerificationHandler.this.getClass());
            }
            if(command.startsWith("authError")){
                verificationController.setAuthorisation(false,  new String(((byte[]) msgArr[4])));
            }
            if(command.startsWith("regOk")){
                regController.showRegScene();
            }
            if(command.startsWith("regError")){
                regController.setRegMsg(new String(((byte[]) msgArr[4])));
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
