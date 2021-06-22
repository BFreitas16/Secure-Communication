package pt.ulisboa.tecnico.sec.secureclient.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.secureclient.exceptions.CommandNotRegisteredException;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

public class CommandDispatcher {
	
	private Map<String, Command> commandMap = new HashMap<>();

	public void register(String commandName, Command command) {
		commandMap.put(commandName, command);
	}
	
	public boolean isValidCommand(String commandName) {
		return commandMap.containsKey(commandName);
	}
	
	public void executeCommand(String line) throws ApplicationException {
		String commandName = extractCommandName(line);
		List<String> commandArguments = extractCommandArguments(line);
		Command command = getCommand(commandName);
		command.execute(commandArguments);
	}
	
	public static String extractCommandName(String line) {
		return line.split(" ")[0];
	}
	
	public static List<String> extractCommandArguments(String line) {
		List<String> arguments = new ArrayList<>();
		
		String[] lineSplitted = line.split(" ");
		for (int i = 1; i < lineSplitted.length; i++) arguments.add(lineSplitted[i]);
		
		return arguments;
	}
	
	private Command getCommand(String commandName) throws CommandNotRegisteredException {
		if (commandName == null)
			throw new CommandNotRegisteredException("[Special Client\"" + SpecialClientApplication.userId + "\"] Command '" + commandName + "' does not exist.");
		
		Command command = commandMap.get(commandName);
		if (command == null)
			throw new CommandNotRegisteredException("[Special Client\"" + SpecialClientApplication.userId + "\"] Command '" + commandName + "' does not exist.");
		
		return command;
	}
	
}
