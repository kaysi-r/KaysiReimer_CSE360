package application;

import databasePart1.DatabaseHelper;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.cell.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import application.Review;
import java.util.Optional;

public class AnswersList {

    private final DatabaseHelper databaseHelper;
    private final Question question;
    private final User user;
    private boolean showOnlyTrustedReviews = false;
    private Map<String, Integer> trustedReviewers = new HashMap<>();

    public AnswersList(DatabaseHelper databaseHelper, Question question, User user) {
        this.databaseHelper = databaseHelper;
        this.question = question;
        this.user = user;
        
        // If user is a student, load their trusted reviewers
        if (user.isStudent()) {
            try {
                List<TrustedReviewer> trusted = databaseHelper.getTrustedReviewers(user.getUserName());
                for (TrustedReviewer t : trusted) {
                    trustedReviewers.put(t.getUserName(), t.getWeight());
                }
            } catch (SQLException e) {
                System.err.println("Error fetching trusted reviewers: " + e.getMessage());
            }
        }
    }
    

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10;");

        // Question section
        Label questionTitle = new Label("Q: " + question.getTitle());
        questionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label questionContent = new Label(question.getContent());

        // ANSWER SECTION
        VBox answersVBox = new VBox(10);

        Label answerSectionLabel = new Label("Answers:");
        answerSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<HBox> answerListView = new ListView<>();
        answerListView.setPrefWidth(560);
        answerListView.setPrefHeight(250);
        loadAnswers(answerListView, primaryStage);

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        TextArea answerInput = new TextArea();
        answerInput.setPromptText("Write your answer...");
        answerInput.setPrefRowCount(6);
        answerInput.setWrapText(true);

        ListView<HBox> suggesting = new ListView<>();
        suggesting.setPrefWidth(100);
        suggesting.setPrefHeight(98);

        Button submitButton = new Button("Submit");

        // Button to access Messaging:
        Button messagesButton = new Button("View Messages for this Question");
        messagesButton.setOnAction(e -> {
            try {
                new PrivateMessages(databaseHelper, question, user).show(primaryStage);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        // Suggestions for answer text (example logic)
        List<String> practicle = List.of("is", "a", "the", "some", "any", "just", "of", "in", "on", "off", "or", "and");

        try {
            List<Answer> answers = databaseHelper.getAnswersForQuestion(question.getQuestionId());
            HashMap<Integer, Integer> check = new HashMap<>();
            for (Answer a : answers)
                check.put(a.getAnswerId(), 0);

            answerInput.textProperty().addListener((observable, oldValue, newValue) -> {
                String[] words = newValue.trim().split("\\s+");
                if (newValue.endsWith(" ") && words.length > 0) {
                    System.out.println("Input word: " + words[words.length - 1]);
                    if (!practicle.contains(words[words.length - 1])) {
                        for (Answer a : answers) {
                            if (a.getAnswerContent().contains(words[words.length - 1]) && check.get(a.getAnswerId()) == 0) {
                                Label answerLabel = new Label(a.getUserName() + ": " + a.getAnswerContent());
                                HBox answerItem = new HBox(10, answerLabel);
                                suggesting.getItems().add(answerItem);
                                check.put(a.getAnswerId(), 1);
                            }
                        }
                    }
                }
            });
        } catch (SQLException e) {
            System.err.println("Error showing answers: " + e.getMessage());
        }

        submitButton.setOnAction(e -> submitAnswer(answerInput, warningLabel, answerListView, primaryStage, suggesting));

        answersVBox.getChildren().addAll(answerSectionLabel, answerListView, answerInput, submitButton, messagesButton, warningLabel, suggesting);

        // REVIEW SECTION (with trusted reviewers prioritized)
        VBox reviewsVBox = new VBox(10);
        Label reviewSectionLabel = new Label("Reviews:");
        reviewSectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<HBox> reviewsListView = new ListView<>();
        reviewsListView.setPrefWidth(240);
        reviewsListView.setPrefHeight(250);
        
        // Add filter checkbox for students to see only trusted reviewer content
        HBox filterBox = new HBox(10);
        if (user.isStudent() && !trustedReviewers.isEmpty()) {
            CheckBox trustedOnlyCheckBox = new CheckBox("Show only trusted reviewers");
            trustedOnlyCheckBox.setSelected(showOnlyTrustedReviews);
            trustedOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showOnlyTrustedReviews = newVal;
                loadReviews(reviewsListView, primaryStage);
            });
            filterBox.getChildren().add(trustedOnlyCheckBox);
            reviewsVBox.getChildren().add(filterBox);
        }
        loadReviews(reviewsListView, primaryStage);

        // If the user is a reviewer, allow them to submit a review.
        if (user.isReviewer()) {
            TextArea reviewInput = new TextArea();
            reviewInput.setPromptText("Enter your review...");
            reviewInput.setPrefRowCount(4);
            reviewInput.setWrapText(true);
            Button reviewSubmitButton = new Button("Submit Review");
            reviewSubmitButton.setOnAction(e -> {
                String reviewText = reviewInput.getText().trim();
                if (!reviewText.isEmpty()) {
                    try {
                        boolean success = databaseHelper.submitReview(question.getQuestionId(), reviewText, user.getUserName());
                        if (success) {
                            System.out.println("Review submitted successfully!");
                            reviewInput.clear();
                            loadReviews(reviewsListView, primaryStage); // refresh review list
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Review cannot be empty!");
                    alert.showAndWait();
                }
            });
            reviewsVBox.getChildren().addAll(reviewSectionLabel, reviewsListView, reviewInput, reviewSubmitButton);
        } else {
            reviewsVBox.getChildren().addAll(reviewSectionLabel, reviewsListView);
        }

        // Combine answer and review sections side by side
        HBox mainContentBox = new HBox(10, answersVBox, reviewsVBox);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new QuestionList(databaseHelper, user).show(primaryStage));

        layout.getChildren().addAll(questionTitle, questionContent, mainContentBox, backButton);
        primaryStage.setScene(new Scene(layout, 800, 500));
        primaryStage.setTitle("Question Detail");
    }
    

    public void loadAnswers(ListView<HBox> answerListView, Stage primaryStage) {
        try {
            List<Answer> answers = databaseHelper.getAnswersForQuestion(question.getQuestionId());
            answerListView.getItems().clear();

            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No answers yet.");
                HBox noAnswersBox = new HBox(10, noAnswersLabel);
                answerListView.getItems().add(noAnswersBox);
                return;
            }

            for (Answer a : answers) {
                Label answerLabel = new Label(a.getUserName() + ": " + a.getAnswerContent());
                HBox answerItem = new HBox(10, answerLabel);

                // Allow editing/deletion for answer's author or admin
                if (a.getUserName().equals(user.getUserName()) || user.isAdmin()) {
                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");

                    editButton.setOnAction(e -> showEditPopup(a, answerListView, primaryStage));
                    deleteButton.setOnAction(e -> deleteAnswer(user, a, answerListView, primaryStage));

                    answerItem.getChildren().addAll(editButton, deleteButton);
                }
                answerListView.getItems().add(answerItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void showReviewsForAnswer(Answer answer, Window window) {
        try {
            // Fetch reviews specifically for this answer - we'll need to add this method to DatabaseHelper
            List<Review> reviews = databaseHelper.getReviewsForAnswer(answer.getAnswerId());
            
            if (reviews.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Reviews");
                alert.setHeaderText(null);
                alert.setContentText("There are no reviews for this answer yet.");
                alert.showAndWait();
                return;
            }
            
            // Create a new stage to display reviews
            Stage reviewStage = new Stage();
            reviewStage.setTitle("Reviews for Answer");
            
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            
            Label headerLabel = new Label("Reviews for answer by " + answer.getUserName());
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            ListView<HBox> reviewListView = new ListView<>();
            
            for (Review review : reviews) {
                HBox reviewItem = createReviewItem(review, reviewStage);
                reviewListView.getItems().add(reviewItem);
            }
            
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> reviewStage.close());
            
            layout.getChildren().addAll(headerLabel, reviewListView, closeButton);
            
            Scene scene = new Scene(layout, 600, 400);
            reviewStage.setScene(scene);
            reviewStage.show();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load reviews: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private HBox createReviewItem(Review review, Stage parentStage) {
        VBox reviewContent = new VBox(5);
        
        Label reviewerLabel = new Label("Reviewer: " + review.getUserName());
        reviewerLabel.setStyle("-fx-font-weight: bold;");
        
        Label contentLabel = new Label(review.getReviewContent());
        contentLabel.setWrapText(true);
        
        reviewContent.getChildren().addAll(reviewerLabel, contentLabel);
        
        Button addToTrustedButton = new Button("Add to Trusted Reviewers");
        
        // Only enable button if this is a student and the reviewer isn't already in their trusted list
        boolean isAlreadyTrusted = false;
        
        if (user.isStudent()) {
            try {
                List<TrustedReviewer> trustedReviewers = databaseHelper.getTrustedReviewers(user.getUserName());
                for (TrustedReviewer tr : trustedReviewers) {
                    if (tr.getUserName().equals(review.getUserName())) {
                        isAlreadyTrusted = true;
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        // Disable button for non-students or if reviewer is already trusted
        addToTrustedButton.setDisable(!user.isStudent() || isAlreadyTrusted || review.getUserName().equals(user.getUserName()));
        
        if (isAlreadyTrusted) {
            addToTrustedButton.setText("Already Trusted");
        } else if (review.getUserName().equals(user.getUserName())) {
            addToTrustedButton.setText("This is you");
        } else if (!user.isStudent()) {
            addToTrustedButton.setText("Only for students");
        }
        
        addToTrustedButton.setOnAction(e -> promptAddToTrustedReviewers(review, parentStage));
        
        HBox reviewItem = new HBox(10, reviewContent, addToTrustedButton);
        HBox.setHgrow(reviewContent, Priority.ALWAYS);
        
        return reviewItem;
    }
    
    private void promptAddToTrustedReviewers(Review review, Stage parentStage) {
        // Create a dialog for setting weight
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add Trusted Reviewer");
        dialog.setHeaderText("Set helpfulness weight for " + review.getUserName());
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create a slider for weight selection
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label instructionLabel = new Label("Set how much you trust this reviewer (1-10):");
        
        Slider weightSlider = new Slider(1, 10, 5);
        weightSlider.setShowTickLabels(true);
        weightSlider.setShowTickMarks(true);
        weightSlider.setMajorTickUnit(1);
        weightSlider.setMinorTickCount(0);
        weightSlider.setSnapToTicks(true);
        
        Label valueLabel = new Label("5");
        weightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.valueOf(newVal.intValue()));
        });
        
        content.getChildren().addAll(instructionLabel, weightSlider, valueLabel);
        dialog.getDialogPane().setContent(content);
        
        // Convert the result to integer weight when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return (int) weightSlider.getValue();
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Integer> result = dialog.showAndWait();
        
        result.ifPresent(weight -> {
            try {
                boolean success = databaseHelper.addTrustedReviewer(user.getUserName(), review.getUserName(), weight);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(review.getUserName() + " has been added to your trusted reviewers with weight " + weight);
                    alert.showAndWait();
                    
                    // Update local trustedReviewers map
                    trustedReviewers.put(review.getUserName(), weight);
                    
                    // Refresh the parent stage to update UI
                    parentStage.close();
                    showReviewsForAnswer(databaseHelper.getAnswerById(review.getAnswerId()), parentStage.getOwner());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to add reviewer. They may not have reviewer permissions.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Database error: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }
    
    private void loadReviews(ListView<HBox> reviewsListView, Stage primaryStage) {
        try {
            List<Review> reviews = databaseHelper.getReviewsForQuestion(question.getQuestionId());
            
            // Filter reviews if showOnlyTrustedReviews is enabled
            if (showOnlyTrustedReviews && user.isStudent() && !trustedReviewers.isEmpty()) {
                reviews = reviews.stream()
                    .filter(r -> trustedReviewers.containsKey(r.getUserName()))
                    .collect(Collectors.toList());
            }

            // Sort reviews so that trusted reviewers (by weight) come first
            Collections.sort(reviews, (r1, r2) -> {
                boolean r1Trusted = trustedReviewers.containsKey(r1.getUserName());
                boolean r2Trusted = trustedReviewers.containsKey(r2.getUserName());
                if (r1Trusted && !r2Trusted) {
                    return -1;
                } else if (!r1Trusted && r2Trusted) {
                    return 1;
                } else if (r1Trusted && r2Trusted) {
                    // Higher weight first
                    return Integer.compare(trustedReviewers.get(r2.getUserName()), trustedReviewers.get(r1.getUserName()));
                } else {
                    // Neither are trusted; maintain natural order (or you can sort by time if available)
                    return 0;
                }
            });

            reviewsListView.getItems().clear();

            if (reviews.isEmpty()) {
                Label noReviewsLabel = new Label(showOnlyTrustedReviews ? 
                    "No reviews from your trusted reviewers." : "No reviews yet.");
                HBox noReviewsBox = new HBox(10, noReviewsLabel);
                reviewsListView.getItems().add(noReviewsBox);
                return;
            }

            for (Review r : reviews) {
                boolean isTrusted = trustedReviewers.containsKey(r.getUserName());
                HBox reviewItem;
                if (isTrusted) {
                    Label trustedLabel = new Label("Trusted (" + trustedReviewers.get(r.getUserName()) + "): ");
                    trustedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    Label reviewLabel = new Label(r.getUserName() + ": " + r.getReviewContent());
                    reviewItem = new HBox(10, trustedLabel, reviewLabel);
                    reviewItem.setStyle("-fx-background-color: #f0fff0;"); // Light green background
                } else {
                    Label reviewLabel = new Label(r.getUserName() + ": " + r.getReviewContent());
                    reviewItem = new HBox(10, reviewLabel);
                }
                reviewsListView.getItems().add(reviewItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void submitAnswer(TextArea answerInput, Label warningLabel, ListView<HBox> answerListView, Stage primaryStage, ListView<HBox> suggesting) {
        String answerText = answerInput.getText().trim();
        if (!answerText.isEmpty()) {
            try {
                List<Answer> answers = databaseHelper.getAnswersForQuestion(question.getQuestionId());
                for (Answer a : answers) {
                    if (a.getAnswerContent().equals(answerText)) {
                        warningLabel.setText("You cannot copy another's answer!");
                        return;
                    }
                }
                Answer newAnswer = new Answer(question.getQuestionId(), user.getUserName(), answerText);
                databaseHelper.addAnswer(newAnswer);
                answerInput.clear();
                loadAnswers(answerListView, primaryStage);
                warningLabel.setText("");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            warningLabel.setText("Answer cannot be empty!");
        }
    }

    public void showEditPopup(Answer answer, ListView<HBox> answerListView, Stage primaryStage) {
        Stage popup = new Stage();
        popup.setTitle("Edit Answer");

        VBox popupLayout = new VBox(10);
        popupLayout.setStyle("-fx-padding: 10;");

        TextArea editInput = new TextArea(answer.getAnswerContent());
        editInput.setPrefSize(400, 200);

        Button saveButton = new Button("Save Changes");

        saveButton.setOnAction(e -> {
            String newContent = editInput.getText().trim();
            if (!newContent.isEmpty()) {
                try {
                    boolean success = databaseHelper.updateAnswer(answer.getAnswerId(), newContent, user);
                    if (success) {
                        loadAnswers(answerListView, primaryStage);
                        popup.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Content cannot be empty!");
                alert.showAndWait();
            }
        });
        popupLayout.getChildren().addAll(new Label("Edit Answer:"), editInput, saveButton);
        Scene popupScene = new Scene(popupLayout, 500, 300);
        popup.setScene(popupScene);
        popup.show();
    }

    private void deleteAnswer(User user, Answer answer, ListView<HBox> answerListView, Stage primaryStage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove your name from this answer?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (user.isAdmin()) {
                        databaseHelper.deleteAnswerPermanently(answer.getAnswerId());
                    } else {
                        databaseHelper.disconnectAnswerFromUser(answer.getAnswerId(), user.getUserName());
                    }
                    loadAnswers(answerListView, primaryStage);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    private void showSubmitReviewDialog(Answer answer) {
        if (!user.isReviewer()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission Denied");
            alert.setHeaderText(null);
            alert.setContentText("Only reviewers can submit reviews for answers.");
            alert.showAndWait();
            return;
        }
        
        // Create a dialog for submitting the review
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Submit Review");
        dialog.setHeaderText("Review answer by " + answer.getUserName());
        
        // Set the button types
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        // Create the review input area
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label answerLabel = new Label("Answer: " + answer.getAnswerContent());
        answerLabel.setWrapText(true);
        
        TextArea reviewInput = new TextArea();
        reviewInput.setPromptText("Enter your review...");
        reviewInput.setPrefRowCount(5);
        reviewInput.setWrapText(true);
        
        content.getChildren().addAll(answerLabel, new Label("Your Review:"), reviewInput);
        dialog.getDialogPane().setContent(content);
        
        // Request focus on the review input by default
        Platform.runLater(() -> reviewInput.requestFocus());
        
        // Convert the result to review text when the submit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return reviewInput.getText().trim();
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(reviewText -> {
            if (reviewText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Empty Review");
                alert.setHeaderText(null);
                alert.setContentText("Review cannot be empty!");
                alert.showAndWait();
                return;
            }
            
            try {
                boolean success = databaseHelper.submitAnswerReview(answer.getAnswerId(), reviewText, user.getUserName());
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your review has been submitted.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to submit review.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Database error: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }
}