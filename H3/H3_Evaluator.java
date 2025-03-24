package questionAnswerEvaluationTestbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The QuestionAnswerEvaluator class simulates an interactive environment where students can ask questions,
 * view potential answers, provide answers to others' questions, mark questions as resolved, and track unresolved questions.
 * It stores questions, answers, and tracks the resolution status.
 */
public class QuestionAnswerEvaluator {

    // Database to store questions and corresponding lists of answers
    private static HashMap<String, List<String>> questionAnswerDatabase = new HashMap<>();
    
    // List to store unresolved questions
    private static List<String> unresolvedQuestions = new ArrayList<>();
    
    // List to store related questions based on keyword matches
    private static List<String> suggestedQuestions = new ArrayList<>();
    
    // Track question statuses (true = resolved, false = unresolved)
    private static HashMap<String, Boolean> questionStatus = new HashMap<>();

    /**
     * Asks a question and returns potential answers if the question is already stored in the system.
     * If no answer is found, the question is added to the list of unresolved questions.
     * 
     * @param question The question asked by the user.
     * @return A string indicating whether potential answers were found or if the question is unresolved.
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
     * Suggests related questions based on a keyword. 
     * Searches the stored questions and returns those containing the specified keyword.
     * 
     * @param keyword The keyword to search for in stored questions.
     * @return A list of questions containing the keyword.
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
     * Returns a list of unresolved questions.
     * 
     * @return A list of unresolved questions.
     */
    public static List<String> viewUnresolvedQuestions() {
        return unresolvedQuestions;
    }

    /**
     * Allows users to provide an answer to a specific question. 
     * Avoids duplicate answers by checking if the answer is already present in the question's answer list.
     * 
     * @param question The question to which the user is providing an answer.
     * @param answer The answer provided by the user.
     * @return A message indicating whether the answer was added or if it already exists.
     */
    public static String provideAnswer(String question, String answer) {
        if (!questionAnswerDatabase.containsKey(question.toLowerCase())) {
            questionAnswerDatabase.put(question.toLowerCase(), new ArrayList<>());
        }
        // Check for duplicate answers
        if (questionAnswerDatabase.get(question.toLowerCase()).contains(answer)) {
            return "This answer is already provided!";
        } else {
            questionAnswerDatabase.get(question.toLowerCase()).add(answer);
            return "Answer added to the question!";
        }
    }

    /**
     * Marks a question as resolved and highlights a specific answer, making it stand out to other users.
     * The question is removed from the unresolved questions list.
     * 
     * @param question The question to be resolved.
     * @param answer The answer that resolves the question.
     * @return A message indicating whether the question was marked as resolved.
     */
    public static String highlightAnswerAndResolveQuestion(String question, String answer) {
        if (questionAnswerDatabase.containsKey(question.toLowerCase()) &&
                questionAnswerDatabase.get(question.toLowerCase()).contains(answer)) {
            questionStatus.put(question, true);  // Mark question as resolved
            unresolvedQuestions.remove(question);  // Remove from unresolved list
            return "Question marked as resolved. Answer highlighted!";
        } else {
            return "Answer not found in the list of answers for this question.";
        }
    }

    /**
     * Checks if a question has been marked as solved.
     * 
     * @param question The question to check.
     * @return True if the question is solved, false otherwise.
     */
    public static boolean isQuestionSolved(String question) {
        return questionStatus.getOrDefault(question, false);
    }
}

