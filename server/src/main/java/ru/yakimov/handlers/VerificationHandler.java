/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.Commands;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.mySql.FilesDB;
import ru.yakimov.mySql.VerificationDB;
import ru.yakimov.utils.MyPackage;

import java.sql.SQLException;

public class VerificationHandler extends ChannelInboundHandlerAdapter {
    private boolean isAuthorisation = false;
    private VerificationDB verificationDB ;
    private FilesDB filesDB ;

    MyPackage myPackage;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        verificationDB = VerificationDB.getInstance();
        filesDB = FilesDB.getInstance();

        myPackage = ((MyPackage) msg);

        if(!myPackage.getType().equals(ProtocolDataType.COMMAND)){
            return;
        }

        String command = new String(myPackage.getCommandArr());
        String data = new String(myPackage.getDataArrForRead());


        if(command.equals(Commands.AUTH.getString())){
            String[] authData = data.split(InProtocolHandler.DATA_DELIMITER,2);
            String login = authData[0];
            String pass = authData[1];
            authorisation(login, pass);

            MyPackage packageSaveLogin = ctx.pipeline().get(InProtocolHandler.class).getPackageController().getActiveElement();
            packageSaveLogin.set(
                    ProtocolDataType.COMMAND,
                    Commands.SAVE_LOGIN,
                    login.getBytes()
                    );

            ctx.fireChannelRead(packageSaveLogin);

        }else if(command.equals(Commands.REG.getString())){
            registration(data);
        }else{
            myPackage.setType(ProtocolDataType.EMPTY);
        }

        ctx.write(myPackage);

        if(isAuthorisation)
            ctx.pipeline().remove(this.getClass());
    }

    private void authorisation(String login, String pass) throws SQLException {

        if(verificationDB.isUser(login, pass)){

            myPackage.set(ProtocolDataType.COMMAND,
                    Commands.AUTH_OK,
                    String.join(InProtocolHandler.UNITS_SEPARATOR, filesDB.getUnitsFromDir(login, InProtocolHandler.ROOT_DIR)).getBytes()
            );

            isAuthorisation = true;
        }else {

            myPackage.set(ProtocolDataType.COMMAND,
                    Commands.AUTH_ERROR,
                    "There is not this user/password".getBytes()
            );
        }
    }


    private void registration(String str) throws SQLException {
        String[] authData = str.split(InProtocolHandler.DATA_DELIMITER,4);
        String login = authData[0];
        String pass = authData[1];
        String eMail = authData[2];
        String controlWord = authData[3];
        if(verificationDB.registration(login,pass,eMail,controlWord)){
            myPackage.set(ProtocolDataType.COMMAND,
                    Commands.REG_OK,
                    "Registration is ok".getBytes()
            );
        }else{

            myPackage.set(ProtocolDataType.COMMAND,
                    Commands.REG_ERROR,
                    "This user exists".getBytes()
            );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }
}
