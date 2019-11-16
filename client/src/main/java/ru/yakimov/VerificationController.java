/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerificationController {

    private boolean isAuthorisation = false;

    public VerificationController(){
    }

    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    @FXML
    private Label loginMsg;

    public void setAuthorisation(boolean isAuthorisation, String msg){
        this.isAuthorisation = isAuthorisation;
        if(isAuthorisation){
            Platform.runLater(this::showMainScene);
            String [] unitsDataArr = msg.split("//%//");
            SceneAssets.getInstance().getController().initializeUnitListView(unitsDataArr);
        }else{
            Platform.runLater(()-> loginMsg.setText(msg));

        }
    }


    public boolean isAuthorisation() {
        return isAuthorisation;
    }

    @FXML
    private void login() {
        String authData = login.getText() + " " + password.getText();
        Connector.getInstance().setAndSendCommand(Commands.AUTH, authData.getBytes());
    }

    private void showMainScene() {

        ((Stage) password.getScene().getWindow()).setScene(SceneAssets.getInstance().getSampleScene());
    }

    @FXML
    private void showRegScene() {

        ((Stage) password.getScene().getWindow()).setScene(SceneAssets.getInstance().getRegScene());
    }

}
