package ru.yakimov;


import com.google.common.primitives.Longs;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.*;

import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import ru.yakimov.handlers.InProtocolHandler;
import ru.yakimov.utils.MyPackage;

import java.io.*;
import java.util.Optional;


public class Controller {

    @FXML
    private TextField path;

    @FXML
    private ListView<Unit> unitListView;

    @FXML
    private void goTo(){
        goToPath(path.getText());
    }


    public void upload(){
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(path.getScene().getWindow());

        System.out.println(selectedFile);

        if(selectedFile == null)
            return;

        sendFile(selectedFile);

    }

    public Controller() {
    }

    private void sendFile(File selectedFile) {

        String startCommand = selectedFile.getName() + InProtocolHandler.DATA_DELIMITER + path.getText();

        Connector.getInstance().setAndSendFile(Commands.START_FILE, startCommand.getBytes());

        MyPackage myPackage = Connector.getInstance().getPackage();

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }



        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(selectedFile))){
            int i = -1;
            int packNumber = 0;

            while ((i = in.read(myPackage.getDataArrForWrite())) != -1){
                packNumber++;
                System.err.println("Send package "+ i + " num# "+packNumber);
                Connector.getInstance()
                        .addToQueue(
                                myPackage
                                    .trimDataArr(i)
                                    .setType(ProtocolDataType.FILE)
                                    .setCommandWithLength(Commands.FILE)
                );
                myPackage = Connector.getInstance().getPackage();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        myPackage.disable();


        Connector.getInstance().setAndSendFile(Commands.END_FILE, Longs.toByteArray(selectedFile.length()));
    }

    public void newFolder(){

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Enter directory name");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name->{
            String data = path.getText() + InProtocolHandler.DATA_DELIMITER + result.get();
            Connector.getInstance().setAndSendCommand(Commands.NEW_FOLDER, data.getBytes());
        });

    }

    @FXML
    private void download(){
        sendSelected(Commands.DOWNLOAD_FILE);
    }

    public void rename(){

    }

    public void delete(){
        sendSelected(Commands.DELETE);
    }

    public void sendSelected(Commands command){
        if(!unitListView.getSelectionModel().isEmpty()){
            Unit unit = unitListView.getSelectionModel().getSelectedItem();
            String data = String.join(InProtocolHandler.DATA_DELIMITER
                    , new String[]{
                            path.getText()
                            , unit.getName()
                            , unit.getExt()
            });
            Connector.getInstance().setAndSendCommand(command, data.getBytes());
        }else{
            showAlertError(command.commandStr+" Error", "No object selected ");

        }
    }

    public void setting(){

    }

    public void goToPath(String path){
        Connector.getInstance().setAndSendCommand(Commands.GO_TO_DIR, path.getBytes());
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

    @FXML
    private void clickIncite(MouseEvent mouseEvent){
        if(mouseEvent.getClickCount() == 2 && !unitListView.getSelectionModel().isEmpty()){
            Unit unit = unitListView.getSelectionModel().getSelectedItem();
            if(unit.isDirectory()){
                goToPath(unit.getPath());
            }if(unit.isBack())
                goToPath(unit.getDirBefore());
        }
    }

    public void initializeUnitListView(String[] units) {
        String parentDir = units[0];

        Platform.runLater(()->{

            unitListView.getItems().clear();

            if(!parentDir.equals(InProtocolHandler.ROOT_DIR)) {
                unitListView.getItems().add(new Unit(parentDir,-1, "","", "", ""));
            }
            path.setText(parentDir);


            for (String unit : units) {
                String[] unitData = unit.split(InProtocolHandler.DATA_DELIMITER, 5);
                if(unitData.length < 5)
                    continue;
                unitListView.getItems().add(new Unit(parentDir, Integer.parseInt(unitData[0]), unitData[1],unitData[2],unitData[3],unitData[4]));

            }
        });

        if(unitListView.getCellFactory() == null)
            unitListView.setCellFactory(studentListView -> new UnitListCell());
    }

    public void showAlertError(String headerText, String contentText){

        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Client");
            alert.setHeaderText( headerText);
            alert.setContentText( contentText);
            alert.showAndWait();
        });
    }

}
