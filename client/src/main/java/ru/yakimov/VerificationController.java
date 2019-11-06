/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import javafx.application.Platform;
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
            String [] unitsDataArr = msg.split("//");
            Connector.getInstance().getController().initializeUnitListView(unitsDataArr);
        }else{
            Platform.runLater(()-> loginMsg.setText(msg));

        }
    }


    public boolean isAuthorisation() {
        return isAuthorisation;
    }

    @FXML
    private void login() {
        Connector connector = Connector.getInstance();

        connector.setCommandProtocol("auth", login.getText() + " " + password.getText());
        connector.send();
    }

    private void showMainScene() {
        Connector connector = Connector.getInstance();

        ((Stage) password.getScene().getWindow()).setScene(connector.getSampleScene());
    }

}
