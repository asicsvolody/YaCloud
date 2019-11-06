package ru.yakimov;


import javafx.fxml.FXML;

import javafx.scene.control.*;

import javafx.util.Callback;

public class Controller {

    @FXML
    private TextField path;

    @FXML
    private ListView<Unit> unitListView;



    public void refresh(){
        System.out.println("Refresh");

    }

    public void load(){
        System.out.println("Load");

    }

    public void upload(){

    }

    public void newFolder(){

    }

    public void rename(){

    }

    public void delete(){

    }

    public void setting(){

    }

    public void goToPath(){

    }

    public void sortName(){

    }

    public void sortExt(){

    }

    public void sortSize(){

    }

    public void sortDate(){

    }

    @FXML
    public void initialize() {


    }





    public void initializeUnitListView(String[] units) {
        unitListView.getItems().clear();
        unitListView.getItems().add(new Unit(-1, "","", "", ""));
        for (String unit : units) {
            String[] unitData = unit.split("\\s", 5);
            unitListView.getItems().add(new Unit(Integer.parseInt(unitData[0]), unitData[1],unitData[2],unitData[3],unitData[4]));
        }
        unitListView.setCellFactory(studentListView -> new UnitListCell());
    }

}
