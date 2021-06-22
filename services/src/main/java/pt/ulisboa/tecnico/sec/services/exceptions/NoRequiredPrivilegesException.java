package pt.ulisboa.tecnico.sec.services.exceptions;

public class NoRequiredPrivilegesException extends ApplicationException {

	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = -1877951056437695628L;
	
	public NoRequiredPrivilegesException(String message) {
		super(message);
	}
	
	public NoRequiredPrivilegesException(String message, Exception e) {
		super(message, e);
	}

}
