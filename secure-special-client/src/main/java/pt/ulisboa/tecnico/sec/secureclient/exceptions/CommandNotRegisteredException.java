package pt.ulisboa.tecnico.sec.secureclient.exceptions;

import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public class CommandNotRegisteredException extends ApplicationException {
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = -2676073532536146142L;

	public CommandNotRegisteredException(String message) {
		super(message);
	}

	public CommandNotRegisteredException(String message, Exception e) {
		super(message, e);
	}

}
