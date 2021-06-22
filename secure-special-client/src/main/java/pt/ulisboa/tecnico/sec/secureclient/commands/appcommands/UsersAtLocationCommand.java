package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;

public class UsersAtLocationCommand extends Command {

	private ISpecialUserService userService = new SpecialUserServiceWithRegisters();
	
	public static final int EXPECTED_ARGUMENTS = 3;

	@Override
	public void execute(List<String> arguments) throws ApplicationException {
		verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
		
		try {
			userService.obtainUsersAtLocation(
						SpecialClientApplication.userId, 
						Integer.parseInt(arguments.get(0)), 
						Integer.parseInt(arguments.get(1)), 
						Integer.parseInt(arguments.get(2))
					);
		} catch (NumberFormatException e) {
			throw new NotSufficientArgumentsException("[Special Client\"" + SpecialClientApplication.userId + 
					"\"] X_location, Y_location and Epoch must be an Integer.");
		}
	}

}
