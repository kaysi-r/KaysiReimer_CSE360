package application;

import java.sql.SQLException;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays all of the requests students have made to become reviewers. It 
 * displays the username of the student who made the request and a short message they
 * left. The instructor can approve or deny the request, or view the student's work
 * to make their decision.
 */
public class ReviewerRequestsPage {
	private final DatabaseHelper databaseHelper;
	private final User user;
	 
	public ReviewerRequestsPage(DatabaseHelper databaseHelper, User user) {
		this.databaseHelper = databaseHelper;   
		this.user = user;
	}
	
	/**
	 * Displays the reviewer requests page. This has a list of requests, which include
	 * the student's username and message, in addition to approve, deny, and see work 
	 * buttons.
	 * @param primaryStage the stage on which to display this page.
	 */
    public void show(Stage primaryStage) {
    	VBox layout = new VBox(10);
    	layout.setStyle("-fx-alignment: center; -fx-padding: 10;");
    	
	    Label userLabel = new Label("Approve or deny students' requests to become reviewers.");
	    userLabel.setStyle("-fx-font-size: 16px;");
	    
	    
		Button logoutButton = new Button("Logout");
       	logoutButton.setOnAction(_ -> {
       		LogOut logOut = new LogOut(databaseHelper, primaryStage);
       		logOut.logout(); 
       	});
		
       	Button backButton = new Button("Back");
       	backButton.setOnAction(_ -> {
       		new InstructorHomePage(databaseHelper, user).show(primaryStage);
       	});
       	
       	
       	ListView<HBox> requestsListView = new ListView<>();

	    try {
	        ObservableList<ReviewerRequest> requests = databaseHelper.getReviewerRequests();

	        for (ReviewerRequest request : requests) {
	            Label requestLabel = new Label(request.getUserName() + ": " + request.getContent());   
	            Button approveButton = new Button("Approve");
	            Button denyButton = new Button("Deny");
	            Button seeWorkButton = new Button("See Work");
	            
	            HBox questionItem = new HBox(10, requestLabel, approveButton, denyButton, seeWorkButton);
            	
	            approveButton.setOnAction(_ -> {
	            	try {
		            	databaseHelper.approveReviewerRequest(request.getUserName());
		            	requestsListView.getItems().remove(questionItem);
	            	} catch (SQLException e) {
	            		e.printStackTrace();
	            	}
	            });
	            denyButton.setOnAction(_ -> {
	            	try {
		            	databaseHelper.denyReviewerRequest(request.getUserName());
		            	requestsListView.getItems().remove(questionItem);
	            	} catch (SQLException e) {
	            		e.printStackTrace();
	            	}
	            });   
	            seeWorkButton.setOnAction(_ -> showStudentWork(request.getUserName()));


	            requestsListView.getItems().add(questionItem);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
      
		
	    layout.getChildren().addAll(userLabel, requestsListView, backButton, logoutButton);
	    
	    Scene reviewerRequestsScene = new Scene(layout, 800, 400);
	  
	    // Set the scene to primary stage
	    primaryStage.setScene(reviewerRequestsScene);
	    primaryStage.setTitle("Reviewer Requests Page");
   	
    }
    
    /**
     * Creates a popup to display the given student's questions and answers.
     * @param userName the student's username.
     */
    private void showStudentWork(String userName) {
    	
    	Stage popup = new Stage();
    	popup.setTitle(userName + "'s Work");
		
		VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");
        
        ListView<HBox> workListView = new ListView<>();
        
        popupLayout.getChildren().addAll(workListView);
        Scene popupScene = new Scene(popupLayout, 500, 300);
        popup.setScene(popupScene);
        
        Button backButton = new Button("Back");
        backButton.setOnAction(_ -> popup.setScene(popupScene));
        
    	try {
    		List<Question> questions = databaseHelper.getQuestionsByUser(userName);
    		
    		for (Question q : questions) {
    			Label titleLabel = new Label(q.getTitle());
    			Button viewButton = new Button("View");
    			
    			HBox questionItem = new HBox(10, titleLabel, viewButton);
    			workListView.getItems().add(questionItem);
    			
    			viewButton.setOnAction(_ -> 
    			popup.setScene(makeQuestionScene(q, backButton)));
    		}
    		
    		List<Answer> answers = databaseHelper.getAnswersByUser(userName);
    		
    		for (Answer a : answers) {
    			Question parentQ = databaseHelper.getQuestionById(a.getQuestionId());
    			
    			Label titleLabel = new Label("Answer for: " + parentQ.getTitle());
    			Button viewButton = new Button("View");
    			
    			HBox questionItem = new HBox(10, titleLabel, viewButton);
    			workListView.getItems().add(questionItem);
    			
    			viewButton.setOnAction(_ -> 
    			popup.setScene(makeAnswerScene(parentQ, a, backButton)));
    		}
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
        popup.show();
    }
    
    /**
     * Makes a scene that displays the given question.
     * @param question the question to display.
     * @param backButton should return the user to the original scene.
     * @return the question scene.
     */
    private Scene makeQuestionScene(Question question, Button backButton) {
    	VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");
        
        Label titleLabel = new Label("Title:");

        TextField titleInput = new TextField(question.getTitle());
        titleInput.setEditable(false);
        
        Label contentLabel = new Label("Content:");

        TextArea contentInput = new TextArea(question.getContent());
        contentInput.setPrefSize(400, 200);
        contentInput.setEditable(false);
        
        popupLayout.getChildren().addAll(titleLabel, titleInput, contentLabel, 
        		contentInput, backButton);
        
        return new Scene(popupLayout, 500, 300);
    }
    
    /**
     * Makes a scene to display the given question and answer pair.
     * @param question the title and content will be displayed.
     * @param answer should be an answer to the given question.
     * @param backButton should return the user to the previous scene.
     * @return the scene.
     */
    private Scene makeAnswerScene(Question question, Answer answer, Button backButton) {
    	VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");
        
        Label titleLabel = new Label("Question Title:");

        TextField titleInput = new TextField(question.getTitle());
        titleInput.setEditable(false);
        
        Label contentLabel = new Label("Question Content:");

        TextArea contentInput = new TextArea(question.getContent());
        contentInput.setPrefSize(400, 200);
        contentInput.setEditable(false);
        
        Label answerLabel = new Label("Answer:");

        TextArea answerInput = new TextArea(answer.getAnswerContent());
        contentInput.setPrefSize(400, 200);
        contentInput.setEditable(false);
        
        popupLayout.getChildren().addAll(titleLabel, titleInput, contentLabel, 
        		contentInput, answerLabel, answerInput, backButton);
        
        return new Scene(popupLayout, 500, 300);
    }
}
