/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

import ru.yakimov.Commands;
import ru.yakimov.ProtocolDataType;

public class YaCloudUtils {

    public static void writeToArrBackCommand(Object[] arrBack, Commands command, String data){
        writeToArrBackCommand(ProtocolDataType.COMMAND,arrBack, command, data.getBytes());


    }

    public static void writeToArrBackCommand(ProtocolDataType packegType, Object[] arrBack, Commands command, byte[] data ){
        arrBack[0] = packegType;

        byte[] commandBack = command.getString().getBytes();
        arrBack[1] = commandBack.length;
        arrBack[2] = commandBack;

        arrBack[3] = data.length;
        arrBack[4] = data;
    }

    public static void writeToArrBackFile(Object[] arrBack, Commands command, byte[] data){
        writeToArrBackCommand(ProtocolDataType.FILE,arrBack, command, data);
    }

    public static void writeToArrBackFile(Object[] arrBack, Commands command, String data){
        writeToArrBackCommand(ProtocolDataType.FILE,arrBack, command, data.getBytes());
    }


}
