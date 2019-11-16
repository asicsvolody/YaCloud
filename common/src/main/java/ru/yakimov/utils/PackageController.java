/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.utils;

public class PackageController extends ObjectPool<MyPackege>{

    @Override
    protected MyPackege newObject() {
        return new MyPackege();
    }

}
