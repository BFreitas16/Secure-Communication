package pt.ulisboa.tecnico.sec.secureclient.exceptions;

import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public class NotSufficientArgumentsException extends ApplicationException {
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = -7291606806555060477L;

	public NotSufficientArgumentsException(String message) {
		super(message);
	}

	public NotSufficientArgumentsException(String message, Exception e) {
		super(message, e);
	}

	

}
