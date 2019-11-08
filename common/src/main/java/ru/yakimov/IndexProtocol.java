/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

public enum IndexProtocol {
    TYPE(0), COMMAND_LENGTH(1), COMMAND(2), DATA_LENGTH(3),DATA(4);

    int index;

    IndexProtocol(int index) {
        this.index = index;
    }

    public int getInt(){
        return index;
    }
}
