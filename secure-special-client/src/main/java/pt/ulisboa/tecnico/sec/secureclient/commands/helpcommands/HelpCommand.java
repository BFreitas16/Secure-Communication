package pt.ulisboa.tecnico.sec.secureclient.commands.helpcommands;

import java.util.List;

import pt.ulisboa.tecnico.sec.secureclient.commands.Command;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.NotSufficientArgumentsException;

public class HelpCommand extends Command {
	
	public static final int EXPECTED_ARGUMENTS = 0;

	@Override
	public void execute(List<String> arguments) throws NotSufficientArgumentsException {
		verifyNumberOfArguments(arguments.size(), EXPECTED_ARGUMENTS);
		
		System.out.println(
				System.lineSeparator() + "## ---------------------------------------------------------- ##" + 
				System.lineSeparator() + "\tSupported Commands are: (all of them using Regular or Atomic Semantics as specified in the requisites)" +
				System.lineSeparator() + "\t* obtainreport <user_id> <epoch>" +
				System.lineSeparator() + "\t* usersatlocation <x_location> <y_location> <epoch>" +
				System.lineSeparator() + "\t* getproofsofuseratepochs <user_id> <epoch_1> <epoch_2> ... <epoch_n>" +
				System.lineSeparator() + "\t* submitreporttest <option>" +
				System.lineSeparator() + "\t* gatherproofstest <option>" +
				System.lineSeparator() + "\t* obtainreporttest <option>" +
				System.lineSeparator() + "\t* broadcasttest <option>" +
				System.lineSeparator() + "\t* registertest <option>" +
				System.lineSeparator() + "\t* help" +
				System.lineSeparator() + "\t* clear" +
				System.lineSeparator() + "\t* quit" +
				System.lineSeparator() + "## ---------------------------------------------------------- ##" +
				System.lineSeparator() + "\tSubmit Report Test Cases:" +
				System.lineSeparator() + "\t 1. Valid Report" +
				System.lineSeparator() + "\t 2. Report with duplicated proofs" +
				System.lineSeparator() + "\t 3. Report duplicated in the same epoch" +
				System.lineSeparator() + "\t 4. Packet sent to the server with repeated nonce (Replay Attack)" +
				System.lineSeparator() + "\t 5. Report with message stealing" +
				System.lineSeparator() + "\t 6. Report with invalid digital signature" +
				System.lineSeparator() + "\t 7. Report with less proofs that necessary" +
				System.lineSeparator() + "\t 8. Report Proofs in different epochs" +
				System.lineSeparator() + "## ---------------------------------------------------------- ##" +
				System.lineSeparator() + "\tSubmit Gather Proofs Test Cases: (Requires client with id 1 online)" +
				System.lineSeparator() + "\t 1. Ask proof out of range" +
				System.lineSeparator() + "\t 2. Ask proof with Replay Attack" +
				System.lineSeparator() + "\t 3. Ask proof with Invalid Digital Signature" +
				System.lineSeparator() + "## ---------------------------------------------------------- ##" + 
				System.lineSeparator() + "\tObtain Reports Test Cases:" + 
				System.lineSeparator() + "\t 1. Obtain Report from invalid user" + 
				System.lineSeparator() + "\t 2. Obtain Report from another user - privilege exception" +
				System.lineSeparator() + "\t 3. Obtain Report with invalid signature" + 
				System.lineSeparator() + "\t 4. Obtain Report with invalid epoch" +
				System.lineSeparator() + "\t 5. Obtain Report with repeated nonce (Replay Attack)" +
				System.lineSeparator() + "## ---------------------------------------------------------- ##" +
				System.lineSeparator() + "\tByzantine Broadcast Test Cases:" +
				System.lineSeparator() + "\t 1. Send message to only 1 server" +
				System.lineSeparator() + "\t 2. Send ECHO and READY from a non-server" +
				System.lineSeparator() + "\t 3. Send different messages to different servers" +
				System.lineSeparator() + "## ---------------------------------------------------------- ##" +
				System.lineSeparator() + "\tByzantine Registers Test Cases: (Requires client with id 1 online)" +
				System.lineSeparator() + "\t 1. Atomic Byzantine Register - sending fake spontaneous read" +
				System.lineSeparator() + "\t Ps: All other cases are covered by the normal functioning of the client, which can be observed when submitting reports or reading reports." +
				System.lineSeparator() + "## ---------------------------------------------------------- ##"
		);
	}

}
