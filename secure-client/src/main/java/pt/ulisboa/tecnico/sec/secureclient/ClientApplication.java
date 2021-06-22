package pt.ulisboa.tecnico.sec.secureclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientApplication {
	
	public static String userId;
	public static int epoch = 0;
	public static int numberOfServers;
	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("[Client\"" + userId + "\"]Need 2 arguments: <port> <userId_integer> <number_of_servers>");
			System.exit(0);
			return;
		}

		try {
			userId = args[1];
			numberOfServers = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.out.println("[Client\"" + userId + "\"] The number of Servers must be an Integer.");
			return;
		}

		SpringApplication.run(ClientApplication.class, args);
	}
	
	public static void incrementEpoch() {
		epoch++;
	}

}
