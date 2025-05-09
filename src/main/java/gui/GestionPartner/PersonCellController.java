package gui.GestionPartner;

import entities.Partner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PersonCellController {

    @FXML
    private HBox Hbox;

    @FXML
    private Label NameLabel;

    @FXML
    private Label TypeLabel;

    @FXML
    private Label ContactInfoLabel;

    @FXML
    private Label VideoLabel;

    // Method to initialize the cell with a Partner object
    public void initializeCell(Partner partner) {
        if (partner != null) {
            NameLabel.setText(partner.getName());
            TypeLabel.setText(partner.getType().toString());
            ContactInfoLabel.setText(partner.getContactInfo());
            VideoLabel.setText(partner.getImagePath());
        }
    }

}
