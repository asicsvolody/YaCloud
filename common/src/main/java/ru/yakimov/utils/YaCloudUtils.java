/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

import ru.yakimov.Commands;
import ru.yakimov.ProtocolDataType;

import java.net.CookieHandler;

public class YaCloudUtils {

    public static void writeToArrBack(Object[] arrBack, Commands command, String data){
        writeToArrBack(arrBack, command, data.getBytes());


    }

    public static void writeToArrBack(Object[] arrBack, Commands command, byte[] data ){
        arrBack[0] = ProtocolDataType.COMMAND;

        byte[] commandBack = command.getString().getBytes();
        arrBack[1] = commandBack.length;
        arrBack[2] = commandBack;

        arrBack[3] = data.length;
        arrBack[4] = data;
    }
}
