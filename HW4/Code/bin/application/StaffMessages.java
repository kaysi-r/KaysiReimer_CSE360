package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

import java.sql.SQLException;
import java.util.List;

public class StaffMessages {
	
	private final DatabaseHelper databaseHelper;
	
	private final User user;
	

	public StaffMessages(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
       
        this.user = user; 
        
    }
    
	 public void show(Stage primaryStage) throws SQLException {
		 
		 VBox layout = new VBox(5);
		    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
		    
		    Label welcomeLabel = new Label("Welcome!! STAFF ISNT COMPLETE");
		    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		    
		    Button quitButton = new Button("Quit");
		    quitButton.setOnAction(a -> {
		    	databaseHelper.closeConnection();
		    	Platform.exit(); 
		    });
		    
		    layout.getChildren().addAll(welcomeLabel, quitButton);
		    Scene welcomeScene = new Scene(layout, 800, 400);

		    // Set the scene to primary stage
		    primaryStage.setScene(welcomeScene);
		    primaryStage.setTitle("UNFINISHED PAGE");
		 
	 }
}
