package pt.ulisboa.tecnico.sec.services.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Clock {
	
	private static Clock INSTANCE;
	
	private Clock() {}
	
	// Lazy building
	public static Clock getInstance() {
		if (INSTANCE == null) INSTANCE = new Clock();
		
		return INSTANCE;
	}

	public LocalDate getDate() { return LocalDate.now(); }
	public LocalTime getTime() { return LocalTime.now(); }
	public LocalDateTime getDateTime() { return LocalDateTime.now(); }

}
