package ru.yakimov;


import com.google.common.primitives.Longs;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.*;

import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import ru.yakimov.handlers.InProtocolHandler;

import java.io.*;
import java.util.Arrays;
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


    public void load(){
        System.out.println("Load");

    }

    public void upload(){
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(path.getScene().getWindow());
        System.out.println(selectedFile);
        sendFile(selectedFile);

    }

    public Controller() {
    }

    private void sendFile(File selectedFile) {
        String startCommand = selectedFile.getName() + InProtocolHandler.DATA_DELIMITER + path.getText();

        Connector.getInstance().setAndSendFile(Commands.START_FILE, startCommand.getBytes());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(selectedFile))){
            byte[] byteArray = new byte[2048];
            int i = -1;
            while ((i = in.read(byteArray)) != -1){
                Connector.getInstance().setAndSendFile(Commands.FILE, Arrays.copyOf(byteArray, i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Connector.getInstance().setAndSendFile(Commands.END_FILE, Longs.toByteArray(selectedFile.length()));

//        try (RandomAccessFile data = new RandomAccessFile(selectedFile, "r")) {
//            byte[] eight = new byte[2048];
//            for (long i = 0, len = data.length() / 2048; i < len; i++) {
//                data.readFully(eight);
//                // do something with the 8 bytes
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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

    public void rename(){

    }

    public void delete(){
        if(!unitListView.getSelectionModel().isEmpty()){
            Unit unit = unitListView.getSelectionModel().getSelectedItem();
            String data = String.join(InProtocolHandler.DATA_DELIMITER, new String[]{path.getText(), unit.getName(), unit.getExt()});
            Connector.getInstance().setAndSendCommand(Commands.DELETE, data.getBytes());
        }else{
            showAlertError("DeleteError", "No object selected ");

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
