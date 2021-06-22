package pt.ulisboa.tecnico.sec.services.exceptions;

public class ProverOutOfRangeException extends ApplicationException {
	
	/**
	 * The serial version id (generated automatically by Eclipse)
	 */
	private static final long serialVersionUID = 3123600585607726608L;

	public ProverOutOfRangeException(String message) {
        super(message);
    }

    public ProverOutOfRangeException(String message, Exception e) {
        super(message, e);
    }
}
