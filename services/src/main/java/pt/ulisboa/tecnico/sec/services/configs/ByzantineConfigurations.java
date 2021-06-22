package pt.ulisboa.tecnico.sec.services.configs;

public class ByzantineConfigurations {
	
    public static final int RANGE = 1; // Grid Range
    
    public static final int MAX_BYZANTINE_USERS = 1; // F
    public static final int MAX_BYZANTINE_FAULTS = 1; // Fs
    public static final int NUMBER_OF_SERVERS = (3 * MAX_BYZANTINE_FAULTS) + 1; // N > 3F
    public static final int MIN_CORRECT_USERS = MAX_BYZANTINE_USERS + 1; // F + 1
    public static final int MINIMUM_BYZ_QUORUM = (3 * MAX_BYZANTINE_USERS); // > 3F + 1
    
}
