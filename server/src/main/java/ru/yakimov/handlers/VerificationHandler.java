/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.IndexProtocol;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.mySql.FilesDB;
import ru.yakimov.mySql.VerificationDB;
import ru.yakimov.utils.YaCloudUtils;

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


        System.out.println(((ProtocolDataType) objArr[IndexProtocol.TYPE.getInt()]).getFirstMessageByte());
        System.out.println(((int) objArr[IndexProtocol.COMMAND_LENGTH.getInt()]));
        System.out.println((new String(((byte[]) objArr[IndexProtocol.COMMAND.getInt()]))));
        System.out.println(((int) objArr[IndexProtocol.DATA_LENGTH.getInt()]));
        System.out.println((new String(((byte[]) objArr[IndexProtocol.DATA.getInt()]))));



        if(!objArr[0].equals(ProtocolDataType.COMMAND)){
            return;
        }

        String command = new String(((byte[]) objArr[2]));
        String data = new String(((byte[]) objArr[4]));


        if(command.equals(Commands.AUTH.getString())){
            String[] authData = data.split(InProtocolHandler.DATA_DELIMITER,2);
            String login = authData[0];
            String pass = authData[1];
            authorisation(login, pass);
            ctx.fireChannelRead(new Object[]{
                    ProtocolDataType.COMMAND,
                    Commands.SAVE_LOGIN.getString().length()
                    ,Commands.SAVE_LOGIN.getString().getBytes()
                    ,login.length()
                    ,login.getBytes()
            });

        }else if(command.equals(Commands.REG.getString())){
            registration(data);

        }else{
            arrBack[0] = ProtocolDataType.EMPTY;
        }

        ctx.write(arrBack);

        if(isAuthorisation)
            ctx.pipeline().remove(this.getClass());
    }

    private void authorisation(String login, String pass) throws SQLException {

        if(verificationDB.isUser(login, pass)){

            YaCloudUtils.writeToArrBack(arrBack, Commands.AUTH_OK
                    ,String.join(InProtocolHandler.UNITS_SEPARATOR, filesDB.getUnitsFromDir(login, InProtocolHandler.ROOT_DIR)));

            isAuthorisation = true;
        }else {
            YaCloudUtils.writeToArrBack(arrBack, Commands.AUTH_ERROR, "There is not this user/password");
        }
    }


    private void registration(String str) throws SQLException {
        String[] authData = str.split(InProtocolHandler.DATA_DELIMITER,4);
        String login = authData[0];
        String pass = authData[1];
        String eMail = authData[2];
        String controlWord = authData[3];
        if(verificationDB.registration(login,pass,eMail,controlWord)){
            YaCloudUtils.writeToArrBack(arrBack, Commands.REG_OK,  "Registration is ok");
        }else{
            YaCloudUtils.writeToArrBack(arrBack, Commands.REG_ERROR,  "This user exists");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
