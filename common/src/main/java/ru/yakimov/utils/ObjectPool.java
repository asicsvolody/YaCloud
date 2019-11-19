package ru.yakimov.utils;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public abstract class ObjectPool<T extends Poolable> {
    protected List<T> activeList;
    protected List<T> freeList;

    public List<T> getActiveList() {
        return activeList;
    }

    protected abstract T newObject();

    public void free(int index) {
        freeList.add(activeList.remove(index));
    }

    public ObjectPool() {
        this.activeList = new ArrayList<>();
        this.freeList = new ArrayList<>();
    }

    public T getActiveElement() {
        while(activeList.size()>=100){
            try {
                Thread.sleep(500);
                checkPool();
                System.err.println("WAITING POOL FREE ELEMENTS");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (freeList.size() == 0) {
            freeList.add(newObject());
        }

        T temp = freeList.remove(freeList.size() - 1);
        activeList.add(temp);
        temp.enable();
        System.out.println("Package number is ---------  "+ temp.getPackageNumber());
        return temp;
    }

    public void freeAll() {
        for (int i = activeList.size() - 1; i >= 0; i--) {
            free(i);
        }
    }

    public void checkPool() {
        for (int i = activeList.size() - 1; i >= 0; i--) {
            if (!activeList.get(i).isActive()) {
                free(i);
            }
        }
    }
}
