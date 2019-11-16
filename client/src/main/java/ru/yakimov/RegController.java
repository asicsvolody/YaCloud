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
import ru.yakimov.handlers.InProtocolHandler;


public class RegController {

    @FXML
    private Label loginMsg;

    @FXML
    private TextField login;

    @FXML
    private PasswordField passwordOne;

    @FXML
    private PasswordField passwordTwo;

    @FXML
    private TextField eMail;

    @FXML
    private TextField controlWord;

    @FXML
    private void registration(){
        if(!isDataCorrect()){
            loginMsg.setText("Data not correct");
            return;
        }

        String data = String.join(InProtocolHandler.DATA_DELIMITER,new String[]{
                login.getText(),
                passwordOne.getText(),
                eMail.getText(),
                controlWord.getText()
        });
        System.out.println(data);
        Connector.getInstance().setAndSendCommand(Commands.REG, data.getBytes());
    }

    private boolean isDataCorrect(){
        return !login.getText().isEmpty() &&
                        !passwordOne.getText().isEmpty() &&
                        !passwordTwo.getText().isEmpty() &&
                        !eMail.getText().isEmpty() &&
                        !controlWord.getText().isEmpty() &&
                        passwordTwo.getText().equals(passwordOne.getText());


    }

    public void setRegMsg(String msg){
        Platform.runLater(() -> loginMsg.setText(msg));
    }



    @FXML
    public void showRegScene() {
        Platform.runLater(()->
            ((Stage) login.getScene().getWindow()).setScene(SceneAssets.getInstance().getVerificationScene())
        );
    }
}
