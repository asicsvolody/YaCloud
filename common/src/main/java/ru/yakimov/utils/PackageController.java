/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

public class PackageController extends ObjectPool<MyPackage>{
    private int packNumber = 0;


    @Override
    protected MyPackage newObject() {
        packNumber++;
        return new MyPackage(packNumber);
    }

}
