package pt.ulisboa.tecnico.sec.secureserver.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;

import java.sql.Timestamp;
import java.util.Date;

@Component
public class EpochTriggerMonitor {

	@Scheduled(fixedRate = 10000, initialDelay = 5000)
	public void publish() {
		Date date = new Date();
		System.out.println("[Server Id: " + ServerApplication.serverId + "] Server heartbeat at " + new Timestamp(date.getTime()));
	}


}
