package pt.ulisboa.tecnico.sec.secureserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import pt.ulisboa.tecnico.sec.secureserver.services.NetworkService;

@SpringBootApplication
@EnableScheduling
public class ServerApplication {
	public static String serverId;
	public static int numberOfServers;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("[Server " + serverId + "] Need 3 arguments: <port> <userId_integer> <number_of_servers>");
			System.exit(0);
			return;
		}

		try {
			serverId = args[1];
			numberOfServers = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.out.println("[Server " + serverId + "] The number of Servers must be an Integer.");
			return;
		}

		// Initializes the threads that listen to "ECHO" and "READY" messages.
		// Allows to remove the synchronized block from the NetworkService.sendBroadcast function
		NetworkService.init();

		SpringApplication.run(ServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

		};
	}
}


