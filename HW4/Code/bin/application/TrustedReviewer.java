package application;

/**
 * The TrustedReviewer class represents a trusted reviewer for a student.
 * It contains the reviewer's username, name, and the weight assigned by the student.
 */
public class TrustedReviewer {
    private String userName;
    private String name;
    private int weight;

    /**
     * Creates a new TrustedReviewer object.
     * @param userName the reviewer's username
     * @param name the reviewer's display name
     * @param weight the weight assigned to the reviewer (1-10)
     */
    public TrustedReviewer(String userName, String name, int weight) {
        this.userName = userName;
        this.name = name;
        this.weight = Math.max(1, Math.min(10, weight)); // Ensure weight is between 1 and 10
    }

    /**
     * Gets the reviewer's username.
     * @return the reviewer's username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the reviewer's display name.
     * @return the reviewer's display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the weight assigned to the reviewer.
     * @return the weight (1-10)
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Sets the weight assigned to the reviewer.
     * @param weight the new weight (1-10)
     */
    public void setWeight(int weight) {
        this.weight = Math.max(1, Math.min(10, weight)); // Ensure weight is between 1 and 10
    }
}