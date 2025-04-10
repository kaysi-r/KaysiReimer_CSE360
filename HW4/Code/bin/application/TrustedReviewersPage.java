package application;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page allows students to view and manage their trusted reviewers.
 * Students can add new reviewers, update their weights, and remove them from their trusted list.
 */
public class TrustedReviewersPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private TableView<TrustedReviewer> reviewersTable;
    private ObservableList<TrustedReviewer> reviewersData;
    private Label statusLabel;
 
    public TrustedReviewersPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;   
        this.currentUser = currentUser;
        this.reviewersData = FXCollections.observableArrayList();
    }
    
    /**
     * Displays the trusted reviewers page on the given stage.
     * @param primaryStage the stage on which to display the page
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        // Header
        Label headerLabel = new Label("Your Trusted Reviewers");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Status label for messages
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #4a86e8;");
        
        // Create table to display trusted reviewers
        setupReviewersTable();
        
        // Load data
        loadTrustedReviewers();
        
        // Buttons for actions
        Button addButton = new Button("Add New Reviewer");
        addButton.setOnAction(e -> showAddReviewerDialog(primaryStage));
        
        Button updateButton = new Button("Update Weight");
        updateButton.setOnAction(e -> updateSelectedReviewerWeight());
        
        Button removeButton = new Button("Remove Reviewer");
        removeButton.setOnAction(e -> removeSelectedReviewer());
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new StudentHomePage(databaseHelper, currentUser).show(primaryStage);
        });
        
        HBox buttonBar = new HBox(10, addButton, updateButton, removeButton);
        
        layout.getChildren().addAll(
            headerLabel, 
            new Label("These reviewers are sorted by how helpful you've rated them (1-10):"),
            reviewersTable, 
            buttonBar,
            statusLabel, 
            backButton
        );
        
        Scene scene = new Scene(layout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Trusted Reviewers");
    }
    
    /**
     * Sets up the table that displays trusted reviewers.
     */
    private void setupReviewersTable() {
        reviewersTable = new TableView<>();
        reviewersTable.setPlaceholder(new Label("You haven't added any trusted reviewers yet."));
        
        TableColumn<TrustedReviewer, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        usernameCol.setPrefWidth(200);
        
        TableColumn<TrustedReviewer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);
        
        TableColumn<TrustedReviewer, Integer> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(100);
        
        reviewersTable.getColumns().addAll(usernameCol, nameCol, weightCol);
        reviewersTable.setItems(reviewersData);
        reviewersTable.setPrefHeight(300);
    }
    
    /**
     * Loads the list of trusted reviewers from the database.
     */
    private void loadTrustedReviewers() {
        try {
            reviewersData.clear();
            List<TrustedReviewer> trustedReviewers = databaseHelper.getTrustedReviewers(currentUser.getUserName());
            reviewersData.addAll(trustedReviewers);
        } catch (SQLException ex) {
            statusLabel.setText("Error loading trusted reviewers: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
    }
    
    /**
     * Shows a dialog to add a new trusted reviewer.
     */
    private void showAddReviewerDialog(Stage primaryStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Add Trusted Reviewer");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        Label label = new Label("Select a reviewer to add to your trusted list:");
        ComboBox<String> reviewerCombo = new ComboBox<>();
        Slider weightSlider = new Slider(1, 10, 5);
        weightSlider.setShowTickLabels(true);
        weightSlider.setShowTickMarks(true);
        weightSlider.setMajorTickUnit(1);
        weightSlider.setMinorTickCount(0);
        weightSlider.setSnapToTicks(true);
        
        Label weightLabel = new Label("Helpfulness Weight: 5");
        weightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            weightLabel.setText("Helpfulness Weight: " + newVal.intValue());
        });
        
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");
        
        HBox buttons = new HBox(10, addButton, cancelButton);
        
        layout.getChildren().addAll(label, reviewerCombo, new Label("How helpful is this reviewer to you?"), 
                                   weightSlider, weightLabel, buttons);
        
        // Populate the combo box with available reviewers
        try {
            List<User> availableReviewers = databaseHelper.getAvailableReviewers(currentUser.getUserName());
            for (User reviewer : availableReviewers) {
                reviewerCombo.getItems().add(reviewer.getUserName() + " (" + reviewer.getName() + ")");
            }
        } catch (SQLException ex) {
            statusLabel.setText("Error loading available reviewers: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            ex.printStackTrace();
        }
        
        // Add action handlers
        addButton.setOnAction(e -> {
            String selectedReviewer = reviewerCombo.getValue();
            if (selectedReviewer == null || selectedReviewer.isEmpty()) {
                showAlert("Please select a reviewer.");
                return;
            }
            
            // Extract username (remove the name part in parentheses)
            String reviewerUsername = selectedReviewer.split(" \\(")[0];
            int weight = (int) weightSlider.getValue();
            
            try {
                boolean success = databaseHelper.addTrustedReviewer(currentUser.getUserName(), reviewerUsername, weight);
                if (success) {
                    loadTrustedReviewers();
                    dialog.close();
                    statusLabel.setText("Trusted reviewer added successfully.");
                    statusLabel.setStyle("-fx-text-fill: #4a86e8;");
                } else {
                    showAlert("Failed to add trusted reviewer. Please try again.");
                }
            } catch (SQLException ex) {
                showAlert("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(layout, 400, 300);
        dialog.setScene(scene);
        dialog.show();
    }
    
    /**
     * Updates the weight of the selected reviewer.
     */
    private void updateSelectedReviewerWeight() {
        TrustedReviewer selectedReviewer = reviewersTable.getSelectionModel().getSelectedItem();
        if (selectedReviewer == null) {
            showAlert("Please select a reviewer to update.");
            return;
        }
        
        // Show dialog to get new weight
        Stage dialog = new Stage();
        dialog.setTitle("Update Weight");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        
        Label label = new Label("Update helpfulness weight for " + selectedReviewer.getName() + ":");
        
        Slider weightSlider = new Slider(1, 10, selectedReviewer.getWeight());
        weightSlider.setShowTickLabels(true);
        weightSlider.setShowTickMarks(true);
        weightSlider.setMajorTickUnit(1);
        weightSlider.setMinorTickCount(0);
        weightSlider.setSnapToTicks(true);
        
        Label weightLabel = new Label("Helpfulness Weight: " + selectedReviewer.getWeight());
        weightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            weightLabel.setText("Helpfulness Weight: " + newVal.intValue());
        });
        
        Button updateButton = new Button("Update");
        Button cancelButton = new Button("Cancel");
        
        HBox buttons = new HBox(10, updateButton, cancelButton);
        
        layout.getChildren().addAll(label, weightSlider, weightLabel, buttons);
        
        // Add action handlers
        updateButton.setOnAction(e -> {
            int newWeight = (int) weightSlider.getValue();
            
            try {
                boolean success = databaseHelper.updateReviewerWeight(
                    currentUser.getUserName(), selectedReviewer.getUserName(), newWeight);
                
                if (success) {
                    loadTrustedReviewers();
                    dialog.close();
                    statusLabel.setText("Reviewer weight updated successfully.");
                    statusLabel.setStyle("-fx-text-fill: #4a86e8;");
                } else {
                    showAlert("Failed to update reviewer weight. Please try again.");
                }
            } catch (SQLException ex) {
                showAlert("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(layout, 400, 200);
        dialog.setScene(scene);
        dialog.show();
    }
    
    /**
     * Removes the selected reviewer from the trusted reviewers list.
     */
    private void removeSelectedReviewer() {
        TrustedReviewer selectedReviewer = reviewersTable.getSelectionModel().getSelectedItem();
        if (selectedReviewer == null) {
            showAlert("Please select a reviewer to remove.");
            return;
        }
        
        // Confirm removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Trusted Reviewer");
        confirm.setContentText("Are you sure you want to remove " + selectedReviewer.getName() + 
                              " from your trusted reviewers list?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = databaseHelper.removeTrustedReviewer(
                        currentUser.getUserName(), selectedReviewer.getUserName());
                    
                    if (success) {
                        loadTrustedReviewers();
                        statusLabel.setText("Reviewer removed successfully.");
                        statusLabel.setStyle("-fx-text-fill: #4a86e8;");
                    } else {
                        showAlert("Failed to remove reviewer. Please try again.");
                    }
                } catch (SQLException ex) {
                    showAlert("Database error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Shows an alert with the given message.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}