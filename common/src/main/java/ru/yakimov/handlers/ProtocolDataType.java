/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;


public enum ProtocolDataType {

    EMPTY((byte)-1), FILE((byte)15), COMMAND((byte)16);

    private byte firstMessageByte;

    ProtocolDataType(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    static ProtocolDataType getDataTypeFromByte(byte b) {
        if (b == FILE.firstMessageByte) {
            return FILE;
        }
        if (b == COMMAND.firstMessageByte) {
            return COMMAND;
        }
        return EMPTY;
    }


    public byte getFirstMessageByte() {
        return firstMessageByte;
    }
}
