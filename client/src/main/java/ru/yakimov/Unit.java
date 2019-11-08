package ru.yakimov;

import java.util.Arrays;

/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

public class Unit {
    enum Style {
        BACK("back.png", -1), FILE("file.png", 1), FOLDER("folder.png", 0);
        String imgFileName;
        int typeInt;

        Style(String imgFileName, int typeInt) {
            this.imgFileName = imgFileName;
            this.typeInt = typeInt;
        }
    }

    private String parenDir;
    private Style style;
    private String name;
    private String ext;
    private String size;
    private String date;

    public Unit(String parenDir, int styleInt, String name, String ext, String size, String date) {
        this.parenDir = parenDir;
        this.style = getStyleFromInt(styleInt);
        this.name = name;
        this.ext = ext;
        this.size = size;
        this.date = date;
    }

    public Style getStyle() {
        return style;
    }

    public String getName() {
        return name;
    }

    public String getExt() {
        return ext;
    }

    public String getSize() {
        return size;
    }

    public String getDate() {
        return date;
    }


    public String getImgFilePath(){
        return "./img/"+style.imgFileName;
    }

    public boolean isDirectory(){
        return style.equals(Style.FOLDER);
    }
    public boolean isBack(){
        return style.equals(Style.BACK);
    }


    public void setStyle(Style style) {
        this.style = style;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPath(){
        if(isDirectory())
            return parenDir+name+"/";
        return parenDir+name+"."+ext;
    }

    public String getDirBefore(){
        String[] dirs = parenDir.split("/");
        if(dirs.length == 1)
            return parenDir;
        return String.join("/",Arrays.copyOf(dirs, dirs.length-1))+"/";
    }

    private Style getStyleFromInt (int i){
        switch (i){
            case -1: return Style.BACK;
            case 0: return Style.FOLDER;
            case 1: return Style.FILE;
        }
        return null;
    }

}
