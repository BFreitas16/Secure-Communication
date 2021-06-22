package pt.ulisboa.tecnico.sec.services.exceptions;

public class SignatureCheckFailedException extends ApplicationException {
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = 6712789933476593181L;

	public SignatureCheckFailedException(String message) {
        super(message);
    }
	
	public SignatureCheckFailedException(String message, Exception e) {
		super(message, e);
	}
    
}
