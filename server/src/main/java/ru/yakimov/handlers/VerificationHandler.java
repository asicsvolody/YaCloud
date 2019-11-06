/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.mySql.FilesDB;
import ru.yakimov.mySql.VerificationDB;

import java.sql.SQLException;

public class VerificationHandler extends ChannelInboundHandlerAdapter {
    private Object[] arrBack = new Object[5];
    private boolean isAuthorisation = false;
    private VerificationDB verificationDB ;
    private FilesDB filesDB ;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        verificationDB = VerificationDB.getInstance();
        filesDB = FilesDB.getInstance();

        Object[] objArr = ((Object[]) msg);


        System.out.println(((ProtocolDataType) objArr[0]).getFirstMessageByte());
        System.out.println(((int) objArr[1]));
        System.out.println((new String(((byte[]) objArr[2]))));
        System.out.println(((int) objArr[3]));
        System.out.println((new String(((byte[]) objArr[4]))));



        if(!objArr[0].equals(ProtocolDataType.COMMAND)){
            return;
        }

        String command = new String(((byte[]) objArr[2]));
        String data = new String(((byte[]) objArr[4]));


        if(command.equals("auth")){
            authorisation(new String(((byte[]) objArr[4])));

        }else if(command.equals("reg")){
            registration(new String(((byte[]) objArr[4])));

        }else{
            arrBack[0] = ProtocolDataType.EMPTY;
        }

        ctx.write(arrBack);

        if(isAuthorisation)
            ctx.pipeline().remove(this.getClass());
    }

    private void authorisation(String str) throws SQLException {
        String[] authData = str.split("\\s",2);
        String login = authData[0];
        String pass = authData[1];
        if(verificationDB.isUser(login, pass)){

            arrBackFillData("authOk"
                    , String.join("//", filesDB.getUnitsFromDir(login, "root")));

            isAuthorisation = true;
        }else {
            arrBackFillData("authError", "There is not this user/password");
        }
    }

    private void arrBackFillData(String command, String data){

        arrBack[0] = ProtocolDataType.COMMAND;

        byte[] commandBack = command.getBytes();
        arrBack[1] = commandBack.length;
        arrBack[2] = commandBack;

        byte[] dataBack = data.getBytes();
        arrBack[3] = dataBack.length;
        arrBack[4] = dataBack;

    }

    private void registration(String str) throws SQLException {
        String[] authData = str.split("\\s",4);
        String login = authData[0];
        String pass = authData[1];
        String eMail = authData[2];
        String controlWord = authData[3];
        if(verificationDB.registration(login,pass,eMail,controlWord)){
            arrBackFillData("regOk",  "Registration is ok");
        }else{
            arrBackFillData("regError",  "This user exists");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
