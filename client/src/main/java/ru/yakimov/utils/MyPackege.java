/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

import ru.yakimov.ProtocolDataType;

import java.util.stream.Stream;

public class MyPackege implements Poolable{

    private boolean isActive;

    private Object[] dataForSend;

    public MyPackege() {
        this.dataForSend = new Object[5];
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void disable(){
        this.isActive = false;
    }

    public void enable(){
        this.isActive = true;
    }

    public Object[] getDataForSend() {
        return dataForSend;
    }

    public void set(ProtocolDataType type, byte[] commandArr, byte[] data){
        dataForSend[0] = type;
        dataForSend[1] = commandArr.length;
        dataForSend[2] = commandArr;
        dataForSend[3] = data.length;
        dataForSend[4] = data;


        Stream.of(dataForSend).forEach(System.out:: println);

    }

}
