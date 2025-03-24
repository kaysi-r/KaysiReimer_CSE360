package questionAnswerEvaluationTestbed;

public class QuestionAnswerTestingAutomation {

    static int numPassed = 0;
    static int numFailed = 0;

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
