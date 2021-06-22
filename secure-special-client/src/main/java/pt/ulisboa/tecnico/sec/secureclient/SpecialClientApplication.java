package pt.ulisboa.tecnico.sec.secureclient;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import pt.ulisboa.tecnico.sec.secureclient.commands.CommandDispatcher;
import pt.ulisboa.tecnico.sec.secureclient.commands.appcommands.*;
import pt.ulisboa.tecnico.sec.secureclient.commands.helpcommands.ClearCommand;
import pt.ulisboa.tecnico.sec.secureclient.commands.helpcommands.HelpCommand;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;

import static java.lang.Thread.sleep;

@SpringBootApplication
public class SpecialClientApplication extends SpringBootServletInitializer implements CommandLineRunner {
	
	@Autowired
	private ApplicationContext context;
	
	private static final CommandDispatcher commandDispatcher = new CommandDispatcher();
	
	public static String userId;
	public static int numberOfServers;
	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("[Special Client\"" + userId + "\"] Need 2 arguments: <port> <userId_integer> <number_of_servers>");
			System.exit(0);
			return;
		}

		try {
			userId = args[1];
			numberOfServers = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.out.println("[Special Client\"" + userId + "\"] The number of Servers must be an Integer.");
			return;
		}

		SpringApplication.run(SpecialClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		sleep(1000);
		try (Scanner in = new Scanner(System.in)) {
			registerCommands(); // registration of commands
			while (true) {
				System.out.print("[Special Client\"" + userId+ "\"] what is the command (type help to show the command list): ");
				String line = in.nextLine().toLowerCase().trim();

				if (line == null || line.isEmpty()) continue;
				
				// UGLY hard coded
				if (CommandDispatcher.extractCommandName(line).equals("quit")) break;
				
				executeCommand(line);
			}
			SpringApplication.exit(context, () -> 0);
		}
	}
	
	private static void executeCommand(String input) {
		try {
			commandDispatcher.executeCommand(input);
		} catch (ApplicationException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void registerCommands() {
		// app commands
		commandDispatcher.register("obtainreport", new ObtainReportCommand());
		commandDispatcher.register("usersatlocation", new UsersAtLocationCommand());
		commandDispatcher.register("getproofsofuseratepochs", new GetProofsOfUserAtEpochsCommand());
		commandDispatcher.register("submitreporttest", new SubmitReportTestCommand());
		commandDispatcher.register("gatherproofstest", new GatherProofsTestCommand());
		commandDispatcher.register("obtainreporttest", new ObtainReportTestCommand());
		commandDispatcher.register("broadcasttest", new ByzantineBroadcastTestCommand());
		commandDispatcher.register("registertest", new ByzantineRegisterTestCommand());

		// help commands
		commandDispatcher.register("help", new HelpCommand());
		commandDispatcher.register("clear", new ClearCommand());
	}

}
