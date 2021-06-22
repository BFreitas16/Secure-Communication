package pt.ulisboa.tecnico.sec.secureclient.commands.helpcommands;

import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;

public class ClearCommand extends Command {
	
	public static final int EXPECTED_ARGUMENTS = 0;

	@Override
	public void execute(List<String> arguments) throws NotSufficientArgumentsException {
		verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
		
		System.out.println(System.lineSeparator().repeat(50));
	}

}
