package pt.ulisboa.tecnico.sec.services.exceptions;

public class InvalidReportException extends ApplicationException {

	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = 8880378425121566371L;

	public InvalidReportException(String message) {
		super(message);
	}
	
	public InvalidReportException(String message, Exception e) {
		super(message, e);
	}
	
}
