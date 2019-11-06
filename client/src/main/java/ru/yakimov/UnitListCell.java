package ru.yakimov;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;


/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

public class UnitListCell extends ListCell<Unit> {
    private FXMLLoader mLLoader;

    public ImageView imgStyle;
    public Label unitName;
    public Label unitExt;
    public Label unitSize;
    public Label unitDate;

    public HBox hbPane;

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    @Override
    protected void updateItem(Unit unit, boolean empty) {
        super.updateItem(unit, empty);
        if (empty || unit == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/unitListViewCell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            imgStyle.setImage(new Image(unit.getImgFilePath()));
            unitName.setText(unit.getName());
            unitExt.setText(unit.getExt());
            unitSize.setText(unit.getSize());
            unitDate.setText(unit.getDate());
            setText(null);
            setGraphic(hbPane);
        }
    }


}
