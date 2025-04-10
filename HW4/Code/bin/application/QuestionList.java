package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

import java.sql.SQLException;
import java.util.List;

public class QuestionList {
	
	private final DatabaseHelper databaseHelper;
	private final User user;

	public QuestionList(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user; 
    }
    
	public void show(Stage primaryStage) {
	    VBox layout = new VBox(10);
	    layout.setStyle("-fx-padding: 10;");

	    Label titleLabel = new Label("All Questions:");
	    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
	    

	    ListView<HBox> questionListView = new ListView<>();

	    try {
	        List<Question> questions = databaseHelper.getAllQuestions();

	        for (Question q : questions) {
	            Label questionLabel = new Label(q.getTitle() + " (by " + q.getUserName() + ")");   
	            Button viewButton = new Button("View");
	            
	            
	            HBox questionItem = new HBox(10, questionLabel, viewButton);
	            
	            // Show button only if user is the author or admin
	            if (q.getUserName().equals(user.getUserName()) || user.isAdmin()) {
	            	Button editButton = new Button("Edit");
		            Button deleteButton = new Button("Delete");
		            Button showReplies = new Button("View Replies");
		            editButton.setOnAction(e -> showEditQuestionPopup(q, questionListView, primaryStage));
		            deleteButton.setOnAction(e -> deleteQuestion(q, questionListView, primaryStage));   
		            showReplies.setOnAction(e -> displayReplies(q, questionListView, primaryStage));   
		            
		            questionItem.getChildren().addAll(showReplies, editButton, deleteButton);
	            }
	            else if (!q.getUserName().equals(user.getUserName())) {
	            	Button replyButton = new Button("Reply");
	            	replyButton.setOnAction(e -> replyQuestionPopup(q, questionListView, primaryStage));
	            	Button showReplies = new Button("View Replies");
	            	showReplies.setOnAction(e -> displayReplies(q, questionListView, primaryStage));   
	            	questionItem.getChildren().addAll(showReplies, replyButton);
	            }
	          

	            viewButton.setOnAction(e -> new AnswersList(databaseHelper, q, user).show(primaryStage));

	            questionListView.getItems().add(questionItem);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    Button backButton = new Button("Back");
	    if (user.isAdmin()) {
	    	backButton.setOnAction(a -> new AdminHomePage(databaseHelper, user).show(primaryStage));
	    }else {
	    	backButton.setOnAction(a -> new StudentHomePage(databaseHelper, user).show(primaryStage));
	    }

	    layout.getChildren().addAll(titleLabel, questionListView, backButton);
	    primaryStage.setScene(new Scene(layout, 800, 400));
	    primaryStage.setTitle("Question List");
	}


	
	private void deleteQuestion(Question question, ListView<HBox> questionListView, Stage primaryStage) {
	    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this question?", ButtonType.YES, ButtonType.NO);
	    confirm.showAndWait().ifPresent(response -> {
	        
	        if (response == ButtonType.YES) {
	            try {
	                if (user.isAdmin()) {
	                    databaseHelper.deleteQuestionPermanently(question.getQuestionId());
	                    System.out.println("Question deleted by Admin.");
	                    //thisLabel.setText("Question permanently deleted 😊");
	                } else {
	                    databaseHelper.disconnectQuestionFromUser(question.getQuestionId(), user.getUserName());
	                    System.out.println("Question disconnected from user.");
	                    //thisLabel.setText("Question disconnected successfully 😊");
	                }

	                new QuestionList(databaseHelper, user).show(primaryStage);

	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        }
	    });
	}

    
    
    private void showEditQuestionPopup(Question question, ListView<HBox> questionListView, Stage primaryStage) {
        Stage popup = new Stage();
        popup.setTitle("Edit Question");

        VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");

        TextField titleInput = new TextField(question.getTitle());
        titleInput.setPromptText("Edit Question Title");

        TextArea contentInput = new TextArea(question.getContent());
        contentInput.setPrefSize(400, 200);

        Button saveButton = new Button("Save Changes");

        saveButton.setOnAction(e -> {
            String newTitle = titleInput.getText().trim();
            String newContent = contentInput.getText().trim();
            if (!newTitle.isEmpty() && !newContent.isEmpty()) {
                try {
                    boolean success = databaseHelper.updateQuestion(question.getQuestionId(), newTitle, newContent);
                    if (success) {
                        System.out.println("Question updated successfully!");
                        popup.close();
                        new QuestionList(databaseHelper, user).show(primaryStage);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
            	Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Title or content cannot be empty!");
                alert.showAndWait();
            }
        });

        popupLayout.getChildren().addAll(new Label("Edit Title:"), titleInput, new Label("Edit Content:"), contentInput, saveButton);
        Scene popupScene = new Scene(popupLayout, 500, 300);
        popup.setScene(popupScene);
        popup.show();
    }

    private void displayReplies(Question question, ListView<HBox> questionListView, Stage primaryStage) {
        try {
            List<Reply> replies = databaseHelper.getRepliesForQuestion(question.getQuestionId());
            questionListView.getItems().clear();
            
            if (replies.isEmpty()) {
                Label noMessagesLabel = new Label("No replies yet.");
                HBox noMessagesBox = new HBox(10, noMessagesLabel);
                questionListView.getItems().add(noMessagesBox);
                return;
            }

            for (Reply m : replies) {
	    		Label messageLabel = new Label(m.getUserName() + ": " + m.getReply());
	    		HBox messageItem = new HBox(10, messageLabel);
	       
	            //Button viewButton = new Button("View");
	            //viewButton.setOnAction(e -> {});   
	            //messageItem.getChildren().addAll(viewButton);
	            questionListView.getItems().add(messageItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void replyQuestionPopup(Question question, ListView<HBox> questionListView, Stage primaryStage) {
        Stage popup = new Stage();
        popup.setTitle("Reply to this question");

        VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");

        TextArea contentInput = new TextArea();
        contentInput.setPrefSize(400, 200);
        
        Button showOriginal = new Button("Show original question");
        Button saveButton = new Button("Save reply");

        saveButton.setOnAction(e -> {
            String replyContent = contentInput.getText().trim();

            if (!replyContent.isEmpty()) {
                try {
                    boolean success = databaseHelper.submitReply(question.getQuestionId(), replyContent, user.getUserName());
                   
                	if (success) {
                        System.out.println("Reply submitted successfully!");
                        popup.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
            	Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Input Error");
                alert.setHeaderText(null);
                alert.setContentText("Reply cannot be empty!");
                alert.showAndWait();
            }
        });
        showOriginal.setOnAction(e -> new AnswersList(databaseHelper, question, user).show(primaryStage));
        popupLayout.getChildren().addAll(new Label("Reply to " + question.getUserName()), contentInput, showOriginal, saveButton);
        Scene popupScene = new Scene(popupLayout, 500, 300);
        popup.setScene(popupScene);
        popup.show();
    }
    
}
