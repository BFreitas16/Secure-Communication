package pt.ulisboa.tecnico.sec.services.exceptions;

import javax.crypto.SecretKey;

/**
 * The top level application exception.
 */
public class ApplicationException extends Exception {

	private SecretKey sessionKey;

	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = -3416001628323171383L;

	
	/**
	 * Creates an exception given an error message
	 * 
	 * @param message The error message
	 */
	public ApplicationException(String message) {
		super (message);
	}
	
	
	/**
	 * Creates an exception wrapping a lower level exception.
	 * 
	 * @param message The error message
	 * @param e The wrapped exception.
	 */
	public ApplicationException(String message, Exception e) {
		super (message, e);
	}

	public void setSecretKey(SecretKey sk) {
		this.sessionKey = sk;
	}

	public SecretKey getSessionKey() {
		return sessionKey;
	}
}
