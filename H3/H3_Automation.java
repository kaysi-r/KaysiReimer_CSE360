package questionAnswerEvaluationTestbed;

/**
 * This class automates testing for the QuestionAnswerEvaluator functionalities.
 * It runs predefined test cases, checks outputs, and counts the number of tests passed or failed.
 */
public class QuestionAnswerTestingAutomation {

    static int numPassed = 0; // Track number of passed tests
    static int numFailed = 0; // Track number of failed tests

    public static void main(String[] args) {

        System.out.println("_____________________________");
        System.out.println("Testing Automation\n");

        // Test Case 1: Ask Question and Receive Potential Answers
        performTestCase(1, "What is inheritance in Java?", false);

        // Test Case 2: View Related Questions and Answers
        QuestionAnswerEvaluator.provideAnswer("What is inheritance in Java?", "Inheritance allows a class to inherit properties from another class.");
        performTestCase(2, "inheritance", true);

        // Test Case 3: View Unresolved Questions
        QuestionAnswerEvaluator.askQuestion("What is polymorphism?");
        performTestCase(3, "unresolved", false);

        // Test Case 4: View and Formulate Own Answer
        performAnswerTest(4, "What is polymorphism?", "Polymorphism allows one interface to be used for multiple implementations.", true);

        // Test Case 5: Highlight Answer and Resolve Question
        performHighlightTest(5, "What is polymorphism?", "Polymorphism allows one interface to be used for multiple implementations.", true);

        System.out.println("___________________________________________");
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    /**
     * Performs a test case for asking questions and receiving potential answers.
     * 
     * @param testCase The test case number.
     * @param inputText The input question or keyword.
     * @param expectedPass Indicates whether the test is expected to pass.
     */
    private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
        System.out.println("Test case " + testCase);
        String result;
        if (inputText.equals("unresolved")) {
            result = QuestionAnswerEvaluator.viewUnresolvedQuestions().toString();
            expectedPass = !result.isEmpty();
        } else {
            result = QuestionAnswerEvaluator.askQuestion(inputText);
        }

        if ((result.contains("Found potential answers") && expectedPass) ||
            (result.contains("Your question has been added") && !expectedPass)) {
            System.out.println("*** Success ***\n");
            numPassed++;
        } else {
            System.out.println("*** Failure ***\n");
            numFailed++;
        }
    }

    /**
     * Performs a test case for providing answers to questions.
     */
    private static void performAnswerTest(int testCase, String question, String answer, boolean expectedPass) {
        System.out.println("Test case " + testCase + ": Provide Answer");
        String result = QuestionAnswerEvaluator.provideAnswer(question, answer);

        if (result.contains("Answer added") && expectedPass) {
            System.out.println("*** Success ***\n");
            numPassed++;
        } else {
            System.out.println("*** Failure ***\n");
            numFailed++;
        }
    }

    /**
     * Performs a test case to highlight an answer and resolve a question.
     */
    private static void performHighlightTest(int testCase, String question, String answer, boolean expectedPass) {
        System.out.println("Test case " + testCase + ": Highlight Answer and Resolve Question");
        String result = QuestionAnswerEvaluator.highlightAnswerAndResolveQuestion(question, answer);

        if (result.contains("Question marked as resolved") && expectedPass) {
            System.out.println("*** Success ***\n");
            numPassed++;
        } else {
            System.out.println("*** Failure ***\n");
            numFailed++;
        }
    }
}

