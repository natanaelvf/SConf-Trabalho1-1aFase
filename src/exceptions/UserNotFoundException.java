package exceptions;

public class UserNotFoundException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8430290272859995878L;

	/**
	 * 
	 */

	public UserNotFoundException(String message) {
        super(message);
    }
}
