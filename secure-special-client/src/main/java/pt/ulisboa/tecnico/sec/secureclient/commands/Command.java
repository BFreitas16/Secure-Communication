package pt.ulisboa.tecnico.sec.secureclient.commands;

import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public abstract class Command {
	
	public abstract void execute(List<String> arguments) throws ApplicationException;
	
	public void verifyNumberOfArguments(int numberOfArguments, int expectedArguments) throws NotSufficientArgumentsException {
		if (numberOfArguments < expectedArguments) 
			throw new NotSufficientArgumentsException("[Special Client\"" + SpecialClientApplication.userId + "\"] Expected " + expectedArguments +  " arguments but found " + numberOfArguments);
	}

}
