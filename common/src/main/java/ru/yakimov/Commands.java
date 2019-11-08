/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

public enum Commands {
    REFRESH("refresh"), NEW_FOLDER("addDir"), GO_TO_DIR("goToDir")
    , DOWNLOAD_FILE("downloadFile"), AUTH("auth"), REG("reg")
    , DELETE("delete"), START_FILE("startFile"), END_FILE("endFile")
    , FILE("file")
    , AUTH_OK("authOk"), REG_OK("regOk"), AUTH_ERROR("authError")
    , REG_ERROR("regError"), SAVE_LOGIN("saveLogin"), ERROR("error");

    String commandStr;

    Commands(String commandStr) {
        this.commandStr = commandStr;
    }

    public String getString() {
        return commandStr;
    }

    public static Commands getCommand(String command){
        if(command.equals("refresh"))
            return REFRESH;
        if(command.equals("addDir"))
            return NEW_FOLDER;
        if(command.equals("goToDir"))
            return GO_TO_DIR;
        if(command.equals("auth"))
            return AUTH;
        if(command.equals("reg"))
            return REG;
        if(command.equals("delete"))
            return DELETE;
        if(command.equals("authOk"))
            return AUTH_OK;
        if(command.equals("regOk"))
            return REG_OK;
        if(command.equals("authError"))
            return AUTH_ERROR;
        if(command.equals("regError"))
            return REG_ERROR;
        if(command.equals("downloadFile"))
            return DOWNLOAD_FILE;
        if(command.equals("saveLogin"))
            return SAVE_LOGIN;
        if(command.equals("error"))
            return ERROR;
        if(command.equals("startFile"))
            return START_FILE;
        if(command.equals("endFile"))
            return END_FILE;
        if(command.equals("file"))
            return FILE;
        return null;
    }

    public static Commands getCommand(byte[] commandArr){
       return getCommand(new String(commandArr));
    }
}
