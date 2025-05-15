package gui.GestionPartner;

import entities.Partner;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PersonCellController {

    @FXML
    private HBox cellContainer;

    @FXML
    private Label nameLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label ratingLabel;

    public void setPartner(Partner partner) {
        nameLabel.setText(partner.getName());
        typeLabel.setText(partner.getType().toString());
        emailLabel.setText(partner.getEmail());
        ratingLabel.setText(String.format("★ %.1f (%d)", partner.getRating(), partner.getRatingCount()));
    }

}
