package application;

/**
 * The Review class represents a review for a question or answer.
 * It stores the review ID, the question/answer ID, the reviewer's username, and the review content.
 */
public class Review {
    private int reviewId;
    private int itemId;       // Either a question_id or answer_id
    private String userName;
    private String reviewContent;
    private boolean isAnswerReview; // Flag to indicate if this is a review for an answer (true) or question (false)

    /**
     * Creates a new Review object with an existing review ID.
     * @param reviewId the review ID
     * @param itemId the question ID or answer ID
     * @param userName the reviewer's username
     * @param reviewContent the content of the review
     * @param isAnswerReview true if this is a review for an answer, false for a question
     */
    public Review(int reviewId, int itemId, String userName, String reviewContent, boolean isAnswerReview) {
        this.reviewId = reviewId;
        this.itemId = itemId;
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.isAnswerReview = isAnswerReview;
    }

    /**
     * Creates a new Review object with an existing review ID for a question (backward compatibility).
     * @param reviewId the review ID
     * @param questionId the question ID
     * @param userName the reviewer's username
     * @param reviewContent the content of the review
     */
    public Review(int reviewId, int questionId, String userName, String reviewContent) {
        this(reviewId, questionId, userName, reviewContent, false);
    }

    /**
     * Creates a new Review object without an assigned review ID.
     * @param itemId the question ID or answer ID
     * @param userName the reviewer's username
     * @param reviewContent the content of the review
     * @param isAnswerReview true if this is a review for an answer, false for a question
     */
    public Review(int itemId, String userName, String reviewContent, boolean isAnswerReview) {
        this.reviewId = -1;
        this.itemId = itemId;
        this.userName = userName;
        this.reviewContent = reviewContent;
        this.isAnswerReview = isAnswerReview;
    }

    /**
     * Creates a new Review object without an assigned review ID for a question (backward compatibility).
     * @param questionId the question ID
     * @param userName the reviewer's username
     * @param reviewContent the content of the review
     */
    public Review(int questionId, String userName, String reviewContent) {
        this(questionId, userName, reviewContent, false);
    }

    /**
     * Gets the review ID.
     * @return the review ID
     */
    public int getReviewId() {
        return reviewId;
    }

    /**
     * Sets the review ID.
     * @param reviewId the new review ID
     */
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Gets the item ID (question ID or answer ID).
     * @return the item ID
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the question ID (for backward compatibility).
     * @return the question ID (same as itemId if this is a question review)
     */
    public int getQuestionId() {
        if (!isAnswerReview) {
            return itemId;
        }
        return -1; // Not applicable for answer reviews
    }

    /**
     * Gets the answer ID.
     * @return the answer ID (same as itemId if this is an answer review)
     */
    public int getAnswerId() {
        if (isAnswerReview) {
            return itemId;
        }
        return -1; // Not applicable for question reviews
    }

    /**
     * Gets the reviewer's username.
     * @return the reviewer's username
     */
    public String getUserName() {
        return userName == null ? "unknown" : userName;
    }

    /**
     * Gets the review content.
     * @return the review content
     */
    public String getReviewContent() {
        return reviewContent;
    }

    /**
     * Checks if this review is for an answer.
     * @return true if this is a review for an answer, false for a question
     */
    public boolean isAnswerReview() {
        return isAnswerReview;
    }
}