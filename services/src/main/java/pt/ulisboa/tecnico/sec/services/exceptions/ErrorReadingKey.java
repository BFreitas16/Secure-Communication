package pt.ulisboa.tecnico.sec.services.exceptions;

public class ErrorReadingKey extends ApplicationException{
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = -6932726633740933616L;

	public ErrorReadingKey(String message) {
        super(message);
    }
	
	public ErrorReadingKey(String message, Exception e) {
		super(message, e);
	}
	
}
