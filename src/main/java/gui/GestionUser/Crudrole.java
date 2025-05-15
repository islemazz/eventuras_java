package gui.GestionUser;

import entities.Role;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.Crole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Crudrole {

    @FXML
    private TableColumn<Role, Integer> role_id;

    @FXML
    private TableColumn<Role, String> role_name;

    @FXML
    private TextField tfrole_id;

    @FXML
    private TextField tfrole_name;

    @FXML
    private TableView<Role> tableview;

    private Crole crole = new Crole();

    @FXML
    public void initialize() {

        try {
            List<Role> roles = crole.afficherAll();
            ObservableList<Role> observableList = FXCollections.observableArrayList(roles);
            tableview.setItems(observableList);

            // Adjust the property names to match your Role model's getter names
            role_id.setCellValueFactory(new PropertyValueFactory<>("roleId"));
            role_name.setCellValueFactory(new PropertyValueFactory<>("roleName"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    @FXML
    void ajouter(ActionEvent event) {
        String role_idText = tfrole_id.getText();
        String role_nameText = tfrole_name.getText();


        // Check if any field is empty
        if (role_idText.isEmpty() || role_nameText.isEmpty()
        ) {

            System.out.println("Error: All fields must be filled before saving.");
            return; // Stop execution if any field is empty
        }

        try {
            // Ensure phone is a valid integer
            int id = Integer.parseInt(role_idText);
            Role newRole = new Role(
                    id,role_nameText
            );

            crole.ajouter(newRole);
            refreshTable();
            System.out.println("User added successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Error: Phone number must be a valid integer.");
        } catch (Exception e) {
            e.printStackTrace(); // Log other unexpected errors
        }

    }

    @FXML
    void clear(ActionEvent event) {

        tfrole_id.setText(null);
        tfrole_name.setText(null);
        tableview.getSelectionModel().clearSelection();

    }

    @FXML
    void delete(ActionEvent event) {
        // Get the selected role from the table
        Role selectedRole = tableview.getSelectionModel().getSelectedItem();

        if (selectedRole == null) {
            System.out.println("Error: No role selected for deletion.");
            return;
        }

        try {
            // Delete the selected role (ensure crole.delete uses the role's ID correctly)
            crole.delete(selectedRole);

            // Refresh the table by fetching the updated list of roles
            List<Role> roles = crole.afficherAll();
            ObservableList<Role> observableList = FXCollections.observableArrayList(roles);
            tableview.setItems(observableList);
            System.out.println("Role deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void getdata(MouseEvent event) {
        Role role = tableview.getSelectionModel().getSelectedItem();
        if (role != null) {
            tfrole_id.setText(String.valueOf(role.getRoleId()));
            tfrole_name.setText(role.getRoleName());
        }

    }

    @FXML
    void modifier(ActionEvent event) {
        if (tableview.getSelectionModel().getSelectedItem() == null) {
            System.out.println("Error: No user selected.");
            return;
        }

        String role_nameText = tfrole_name.getText();
        String role_idText = tfrole_id.getText();


        // Check if any field is empty
        if ( role_nameText.isEmpty() || role_idText.isEmpty()) {

            System.out.println("Error: All fields must be filled before updating.");
            return;
        }



        try {
            // Ensure phone and ID are valid integers
            int role_id = Integer.parseInt(role_idText);

            Role updatedRole = new Role(
                    role_id,role_nameText
            );

            crole.updateRole(updatedRole);
            refreshTable();
            System.out.println("role updated successfully!");

        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be valid integer.");
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    @FXML
    private void refreshTable() {
        try {
            ObservableList<Role> observableList = FXCollections.observableList(crole.afficherAll());
            tableview.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void goto_user(ActionEvent event) {
    }

    public void goto_event(ActionEvent event) {
    }

    public void goto_forum(ActionEvent event) {
    }

    public void goto_shop(ActionEvent event) {
    }

    public void goto_blog(ActionEvent event) {
    }

    public void goto_edit(ActionEvent event) {
    }

    public void disconnect(ActionEvent event) {

    }

        public void goto_dashboard(ActionEvent e) throws IOException { navigateTo("/adminDashboard.fxml", e); }

    private void navigateTo(String path, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    }






