package exceptions;

public class UserNotOwnerException extends Exception {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 8394461384114214971L;

	/**
	 * 
	 */

	public UserNotOwnerException(String message) {
        super(message);
    }
}