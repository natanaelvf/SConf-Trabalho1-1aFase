package exceptions;

public class InsuficientFundsException  extends Exception {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 7788582533899840441L;

	public InsuficientFundsException(String message) {
        super(message);
    }
}