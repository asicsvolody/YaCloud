/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

import com.sun.deploy.security.ValidationState;
import ru.yakimov.Commands;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.handlers.InProtocolHandler;

import java.util.Arrays;
import java.util.stream.Stream;

public class MyPackage implements Poolable{

    private static final int TYPE           = 0;
    private static final int COMMAND_LENGTH = 1;
    private static final int COMMAND        = 2;
    private static final int DATA_LENGTH    = 3;
    private static final int DATA           = 4;


    private boolean isActive;

    private final byte[] dataArr ;

    private final Object[] dataObjArr;

    public MyPackage() {
        this.dataObjArr = new Object[5];
        this.dataArr = new byte[InProtocolHandler.DATA_MAX_SIZE];
        dataObjArr[4] = dataArr;

    }


    @Override
    public boolean isActive() {
        return isActive;
    }

    public void disable(){
        dataObjArr[TYPE] = null;
        dataObjArr[COMMAND_LENGTH] = null;
        dataObjArr[COMMAND] = null;
        dataObjArr[DATA_LENGTH] = null;
        dataObjArr[DATA] = null;
        this.isActive = false;
    }


    public void enable(){
        dataObjArr[DATA] = dataArr;

        this.isActive = true;
    }

    public Object[] getDataObjArr() {
        return dataObjArr;
    }

    public void set(ProtocolDataType type, byte[] commandArr, byte[] data){
        dataObjArr[TYPE] = type;
        dataObjArr[COMMAND_LENGTH] = commandArr.length;
        dataObjArr[COMMAND] = commandArr;
        setDataWithLength(data);



        Stream.of(dataObjArr).forEach(System.out:: println);

    }

    public void set(ProtocolDataType type, Commands command, byte[] data){
        set(type, command.getString().getBytes(), data);
    }

    public void setType(ProtocolDataType type){
        dataObjArr[TYPE] = type;
    }

    public void setCommandLength(int length){
        dataObjArr[COMMAND_LENGTH] = length;
    }

    public void setCommandArr(byte[] commandArr){
        dataObjArr[COMMAND] = commandArr;
    }

    public void setDataLength(int length){
        dataObjArr[DATA_LENGTH] = length;
    }

    public void setData(byte[] data){
        if(data.length < InProtocolHandler.DATA_MAX_SIZE) {
            dataObjArr[DATA] = data;
            return;
        }
        System.arraycopy(data, 0, dataArr, 0, data.length);
        dataObjArr[DATA] = dataObjArr;

    }

    public void setCommandWithLength(Commands command){
        byte[] commandArr = command.getString().getBytes();
        setCommandLength(commandArr.length);
        setCommandArr(commandArr);

    }

    public void setDataWithLength(byte[] data){
        setDataLength(data.length);
        setData(data);
    }

    public ProtocolDataType getType(){
        return ((ProtocolDataType) dataObjArr[TYPE]);
    }

    public int getCommandLength(){
        return ((int) dataObjArr[COMMAND_LENGTH]);
    }

    public byte[] getCommandArr(){
        return (byte[]) dataObjArr[COMMAND];
    }

    public int getDataLength(){
        return ((int) dataObjArr[DATA_LENGTH]);
    }

    public byte[] getDataArrForWrite(int length){
        if(length < InProtocolHandler.DATA_MAX_SIZE)
            dataObjArr[DATA] = Arrays.copyOf(((byte[]) dataObjArr[DATA]), length);
        return ((byte[]) dataObjArr[DATA]);
    }

    public byte[] getDataArrForWrite(){
        dataObjArr[4] = dataArr;
        return ((byte[]) dataObjArr[DATA]);
    }





    public byte[] getDataArrForRead(){
        byte[] data = (byte[]) dataObjArr[DATA];
        int size = (int) dataObjArr[DATA_LENGTH];

        if(data.length > size)
            return Arrays.copyOf(data, size);

        return (byte[]) dataObjArr[DATA];

    }

    public void trimDataArr(int length){
        dataObjArr[DATA_LENGTH] = length;
        dataObjArr[DATA] = Arrays.copyOf(((byte[]) dataObjArr[DATA]), length);

    }




}
