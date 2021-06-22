package pt.ulisboa.tecnico.sec.secureclient.commands.appcommands;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;
import pt.ulisboa.tecnico.sec.secureclient.services.SpecialUserServiceWithRegisters;
import pt.ulisboa.tecnico.sec.services.dto.ResponseUserProofsDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.interfaces.ISpecialUserService;

public class GetProofsOfUserAtEpochsCommand extends Command {
	
	private ISpecialUserService userService = new SpecialUserServiceWithRegisters();
	
	public static final int EXPECTED_ARGUMENTS = 2; // in this case is the minimum 

	@Override
	public void execute(List<String> arguments) throws ApplicationException {
		verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
		
		try {
			List<Integer> epochs = new ArrayList<>();
			for (int i = 1; i < arguments.size(); i++) {
				addNumberToListOfEpochs(epochs, arguments.get(i));
			}
			ResponseUserProofsDTO response  = userService.requestMyProofs(SpecialClientApplication.userId, arguments.get(0), epochs);
			System.out.println(response);

		} catch (NumberFormatException e) {
			throw new NotSufficientArgumentsException("[Special Client\"" + SpecialClientApplication.userId + 
					"\"] X_location, Y_location and Epoch must be an Integer.");
		}
	}
	
	private void addNumberToListOfEpochs(List<Integer> epochs, String possibleNumber) {
		try {
			epochs.add(Integer.parseInt(possibleNumber));
		} catch (NumberFormatException e) {
			// nothing, just ignore it
		}
	}

}
