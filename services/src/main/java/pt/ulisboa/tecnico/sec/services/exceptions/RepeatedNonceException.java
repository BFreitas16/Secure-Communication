package pt.ulisboa.tecnico.sec.services.exceptions;

/**
 * Exception thrown when an repeated nonce is detected
 */
public class RepeatedNonceException extends ApplicationException {

	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = 1338194157927932280L;

	public RepeatedNonceException(String message) {
        super(message);
    }
}
