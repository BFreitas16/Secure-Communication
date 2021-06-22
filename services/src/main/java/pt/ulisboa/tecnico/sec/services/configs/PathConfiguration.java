package pt.ulisboa.tecnico.sec.services.configs;

public class PathConfiguration {
	
	private PathConfiguration() {}
	
	// protocol used to access the endpoints
	public static final String ACCESS_PROTOCOL = "http";
	// hostname (without endpoints)
	public static final String HOST = ACCESS_PROTOCOL + "://127.0.0.1";

	// client and server base ports
	
	public static final int SERVER_PORT_BASE = 9200;
	public static final int CLIENT_PORT_BASE = 9000;

	// endpoints
	
	public static final String GET_REPORT_ENDPOINT =  "/getReport";
	public static final String SUBMIT_REPORT_ENDPOINT =  "/submitReport";
	public static final String OBTAIN_USERS_AT_LOCATION_EPOCH_ENDPOINT = "/locations/management/";
	public static final String GET_PROOFS_AT_EPOCHS_ENDPOINT = "/getProofs";
	public static final String SERVER_ECHO_ENDPOINT = "/echo/{sendingServerId}";
	public static final String SERVER_READY_ENDPOINT = "/ready/{sendingServerId}";
	public static final String SERVER_ECHO = "/echo/";
	public static final String SERVER_READY = "/ready/";

	public static final String SPONTANEOUS_READ_ATOMIC_REGISTER_ENDPOINT = "/spontaneousRead/";
	public static final String SPONTANEOUS_READ_ATOMIC_REGISTER = "/spontaneousRead/{serverId}";
	public static final String READ_COMPLETE_ENDPOINT = "/readComplete";

	// Key Paths
	
	public static final String MAIN_DIR = System.getProperty("user.dir");
	public static final String KEYSTORE_LOCATION = MAIN_DIR + "/secKeystore.jks";
	public static final String CLIENT_KEY_FOLDER = MAIN_DIR + "/services/src/main/java/pt/ulisboa/tecnico/sec/services/keys/client";
	public static final String SERVER_KEY_FOLDER = MAIN_DIR + "/services/src/main/java/pt/ulisboa/tecnico/sec/services/keys/server";
	
	// Auxiliary functions to build the URLs
	
	public static String buildUrl(String host, String endpoint) { return host + endpoint; }
	
	public static String getServerUrl(int server) { return HOST + ":" + (SERVER_PORT_BASE + server); }
	public static String getClientURL(int client) { return buildUrl(HOST + ":" + (9000 + client), "/proof"); }
	public static String getSpontaneousReadURL(int client, int serverId) { return buildUrl(HOST + ":" + (9000 + client), SPONTANEOUS_READ_ATOMIC_REGISTER_ENDPOINT + serverId); }

	public static String getGetReportURL(int server) { return buildUrl(getServerUrl(server), GET_REPORT_ENDPOINT); }
	public static String getSubmitReportURL(int server) { return buildUrl(getServerUrl(server), SUBMIT_REPORT_ENDPOINT); }
	public static String getObtainUsersAtLocationEpochURL(int server) { return buildUrl(getServerUrl(server), OBTAIN_USERS_AT_LOCATION_EPOCH_ENDPOINT); }
	public static String getGetProofsAtEpochsURL(int server) { return buildUrl(getServerUrl(server), GET_PROOFS_AT_EPOCHS_ENDPOINT); }

	public static String getServerPublicKey(String serverId) { return SERVER_KEY_FOLDER + "/S" + serverId + "pub.key"; }
	public static String getServerPrivateKey(String serverId) { return SERVER_KEY_FOLDER + "/S" + serverId + "priv.key"; }

}
