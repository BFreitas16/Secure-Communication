package pt.ulisboa.tecnico.sec.services.exceptions;

public class InvalidRequestException extends ApplicationException {
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = 1509028736295282576L;

	public InvalidRequestException(String message) {
        super(message);
    }
}
