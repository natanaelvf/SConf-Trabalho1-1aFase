package exceptions;

public class InvalidPasswordException  extends Exception {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 328579827899840441L;

	public InvalidPasswordException(String message) {
        super(message);
    }
}