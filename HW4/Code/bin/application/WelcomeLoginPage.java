package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    ComboBox<String> roleSelector = new ComboBox<>();
	    user.createSingleRoleSelector(roleSelector);
	    roleSelector.setMaxWidth(100);
	    //select first item by default so that box is never empty
	    roleSelector.getSelectionModel().select(0);
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	    	user.printRoles();
	    	
	    	String selectedRole = roleSelector.getSelectionModel().getSelectedItem();
	    	
	    	if (selectedRole.equals("admin")) {
	    		new AdminHomePage(databaseHelper, user).show(primaryStage);
	    	} else if (selectedRole.equals("instructor")) {
	    		new InstructorHomePage(databaseHelper, user).show(primaryStage);
	    	} else if (selectedRole.equals("student")) {
	    		new StudentHomePage(databaseHelper, user).show(primaryStage);
	    	} else if (selectedRole.equals("reviewer")) {
	    		System.out.print("Reviewer was selected, but hasn't been implemented yet.");
	    	} else { //staff
	    		System.out.print("Staff was selected, but hasn't been implemented yet.");
	    	}
	    });
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });

	    layout.getChildren().addAll(welcomeLabel, roleSelector, continueButton, quitButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}
