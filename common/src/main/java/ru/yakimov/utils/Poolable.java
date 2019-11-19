package ru.yakimov.utils;

public interface Poolable {
    boolean isActive();
    void enable();
    void disable();
    int getPackageNumber();
}

