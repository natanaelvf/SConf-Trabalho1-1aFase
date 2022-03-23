package exceptions;

public class InvalidUserIdException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 128349081928739274L;
	
	public InvalidUserIdException(String message) {
        super(message);
    }


}
