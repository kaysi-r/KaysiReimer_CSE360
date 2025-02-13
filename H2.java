import java.util.*;

// Main Application Entry Point
public class HW2 {
    public static void main(String[] args) {
        QuestionAnswerSystem system = new QuestionAnswerSystem();
        system.run();
        
        //similar to CSE240 format
    }
}

// Class representing a Question
class Question {
	//how the forum will work:
		//Each question will be listed in terms of a number
			//Each question will have all answeres following them in order of posting
    	//Each question will either be resolved or not resolved
			//for sake of this anyone can resolve it
	private static int QuestionCounter = 1;
    private int QuestionNumber;
    private String text;
    private boolean resolved;
    private List<String> answers;

    public Question(String text) {
    	//puts all questions into an ordered list 
    	//oldest questions first
        this.QuestionNumber = QuestionCounter++;
	//put all answers to each question into its own array
        //one array per question
        this.answers = new ArrayList<>();    
        //text input for each question
        this.text = text;
        //set all resolutions to questions as not resolved upon posting
        this.resolved = false;
        
    }
    
////////////////////////////////////////////////////
    //all parts of a question: number, answers, text, resolutions
    public int getQuestionNumber() { 
    	return QuestionNumber; }
    public String getText() { 
    	return text; }
    public boolean isResolved() {
    	return resolved; }
    public List<String> getAnswers() {
    	return answers; }
    public void addAnswer(String answer) {
        answers.add(answer);}
    public void markAsResolved() {
        this.resolved = true;}
}
////////////////////////////////////////////////////

// Question and Answer System
class QuestionAnswerSystem {
    private List<Question> questions;
    private Scanner scanner;

    public QuestionAnswerSystem() {
        this.questions = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
        	System.out.println();
        	System.out.println();
        	System.out.println("Option 1) Create a Question");
            System.out.println("Option 2) Answer One of the Questions");
            System.out.println("Option 3) View List of Questions");
            System.out.println("Option 4) Mark a Question as Resolved");
            System.out.println("Option 5) Quit / LogOut");
            System.out.print("What would you like to do today?: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); 
            
            switch (choice) {
                case 1:
                    askQuestion(); //see below
                    break;
                case 2:
                    answerQuestion();  //see below
                    break;
                case 3:
                    viewQuestions();  //see below
                    break;
                case 4:
                    markResolved();  //see below
                    break;
                case 5:
                    System.out.println("Logging Out ...");
                    return; //ends being able to do anything
                default: //not option 1-5
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private void askQuestion() {
        System.out.print("Enter your question: ");
        System.out.println();
        
        String text = scanner.nextLine().trim();
        if (!text.isEmpty()) {
            questions.add(new Question(text));
            //question passed into forum..positive test case
            System.out.println("Question added.");
        } else { //negative test case...no text input
            System.out.println("Question cannot be empty. Input Valid Text!");
        }  }

    private void viewQuestions() {
    	//Format questions
        for (Question quest : questions) {
        	int count = 0; //set to 0 as immediate increase to 1 when counting questions
            System.out.println("#" + quest.getQuestionNumber() + ") Question = " + quest.getText() + " . . . " + (quest.isResolved() ? "QUESTION RESOLVED" : "QUESTION UNRESOLVED") + "!");
            for (String answer : quest.getAnswers()) {
            	++count;
            	 //format for displaying a multiple number of question answer responses
                System.out.println("   Possible Answer " + count + ": " + answer);
                
            }
        } }

    private void answerQuestion() {
        System.out.print("Enter question Number you wish to Answer: ");
        
        int QuestionNumber = scanner.nextInt();
        scanner.nextLine();
        
        for (Question quest : questions) {
        	//goes through all questions
            if (quest.getQuestionNumber() == QuestionNumber) {
                System.out.print("Enter your answer: ");
                String answer = scanner.nextLine().trim();
                //receive input
                if (!answer.isEmpty()) {
                	//if text line actually contains text
                	quest.addAnswer(answer);
                    System.out.println("Answer has been added.");
                } else {
                	//when input contains no characters within/blank repsonse
                    System.out.println("Answer cannot be empty. Enter proper answer.");
                }
                return;
            }
        }
        
        System.out.println("Question Number not found. Please enter a valid Question Number from the List");
    }

    private void markResolved() {
        System.out.print("Enter question to be mark as resolved: ");
        //selects question from numbered list oldest to newest
        int QuestionNumber = scanner.nextInt();
        for (Question quest : questions) {
        	//only access if valid question
            if (quest.getQuestionNumber() == QuestionNumber) {
            	//sets resolution to true
            	quest.markAsResolved();
                System.out.println("Question has been marked as resolved.");
                return;
            }
        }
        //Ex. choosing Question #9 when only 3 questions actually exist
        System.out.println("Question not found. Please enter a valid Question Number from the list.");
    }
}
