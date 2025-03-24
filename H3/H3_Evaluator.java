package questionAnswerEvaluationTestbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionAnswerEvaluator {

    private static HashMap<String, List<String>> questionAnswerDatabase = new HashMap<>(); // Store questions with lists of answers
    private static List<String> unresolvedQuestions = new ArrayList<>(); // Track unresolved questions
    private static List<String> suggestedQuestions = new ArrayList<>();  // Store related questions based on keywords
    private static HashMap<String, Boolean> questionStatus = new HashMap<>(); // Track if a question is solved

    /**
     * Method to ask a question and get potential answers.
     */
    public static String askQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        if (questionAnswerDatabase.containsKey(lowerQuestion)) {
            return "Found potential answers: " + questionAnswerDatabase.get(lowerQuestion);
        } else {
            unresolvedQuestions.add(question);
            questionStatus.put(question, false);  // Mark as unresolved
            return "No answer found. Your question has been added to unresolved questions.";
        }
    }

    /**
     * Method to view related questions based on keywords.
     */
    public static List<String> viewRelatedQuestions(String keyword) {
        suggestedQuestions.clear();
        for (String question : questionAnswerDatabase.keySet()) {
            if (question.contains(keyword.toLowerCase())) {
                suggestedQuestions.add(question);
            }
        }
        return suggestedQuestions;
    }

    /**
     * Method to view unresolved questions.
     */
    public static List<String> viewUnresolvedQuestions() {
        return unresolvedQuestions;
    }

    /**
     * Method to provide and formulate an answer.
     */
    public static String provideAnswer(String question, String answer) {
        if (!questionAnswerDatabase.containsKey(question.toLowerCase())) {
            questionAnswerDatabase.put(question.toLowerCase(), new ArrayList<>());
        }
        // Check if answer already exists
        if (questionAnswerDatabase.get(question.toLowerCase()).contains(answer)) {
            return "This answer is already provided!";
        } else {
            questionAnswerDatabase.get(question.toLowerCase()).add(answer);
            return "Answer added to the question!";
        }
    }

    /**
     * Method to mark a question as resolved and highlight an answer.
     */
    public static String highlightAnswerAndResolveQuestion(String question, String answer) {
        if (questionAnswerDatabase.containsKey(question.toLowerCase()) &&
                questionAnswerDatabase.get(question.toLowerCase()).contains(answer)) {
            questionStatus.put(question, true);  // Mark as resolved
            unresolvedQuestions.remove(question);  // Remove from unresolved questions
            return "Question marked as resolved. Answer highlighted!";
        } else {
            return "Answer not found in the list of answers for this question.";
        }
    }

    /**
     * Method to view if a question is solved.
     */
    public static boolean isQuestionSolved(String question) {
        return questionStatus.getOrDefault(question, false);
    }
}
