package pt.ulisboa.tecnico.sec.secureserver.services;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.UnreachableClientException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service used by the operations: obtainLocationReport & submitLocationReport
 * As the obtainLocationReport should return the same information on all
 * correct server processes if it was delivered by a correct server process.
 */
public class ByzantineAtomicRegisterService {
	
	private ByzantineAtomicRegisterService() {}
	
    // Send HTTP requests
    private static RestTemplate restTemplate = new RestTemplate();

    // Timestamp used during the write part of the protocol
    private static AtomicLong timestamp = new AtomicLong(0);

    // Listening readers, used to notify registered reading processes, when an concurrent write occurs
    // String is the clientId that sent the read request and integer the RID.
    private static ConcurrentHashMap<String, Integer> listening = new ConcurrentHashMap<>();

    /**
     *  Used when submitLocationReport is called, to write to the database.
     *
     *  If there are listeners registered for obtainLocationReport, notify them with the received value.
     *
     *  TODO: Timestamp usage esta sketchy as hell
     */
    public static AcknowledgeDto receiveWriteRequest(String userId, ReportDTO report, long requestTimestamp, UserService userService) throws ApplicationException {
        // If the timestamp of the request is higher than the timestamp kept track by the server
        // then the writer message is newer, if so write the value and update the timestamp.
        // But can't a malicious attacker send a very big timestamp and make the server always return the same value
        // afterwards?
        if(requestTimestamp > timestamp.get()) {
            timestamp.set(requestTimestamp);
            userService.submitLocationReport(userId, report);
        }


        // Send message to all registered listening processes
        for (String s : listening.keySet()) {
            // Build path, attention this will probably generate errors and confusion, the path is generated
            // by adding the "s" value (clientId) with the base port "9000", obtaining the port on which the client
            // process is running, then using as hostname the localhost and endpoint, the configured in PathConfig.
            String host = PathConfiguration.HOST + ":" + String.valueOf(PathConfiguration.CLIENT_PORT_BASE + Integer.parseInt(s));
            String endpoint = PathConfiguration.SPONTANEOUS_READ_ATOMIC_REGISTER_ENDPOINT + ServerApplication.serverId;
            String url = PathConfiguration.buildUrl(host, endpoint);

            System.out.println("[Server Id: " + ServerApplication.serverId + "] Byzantine Atomic Register, sending spontaneous read to clientId: " + s + " with url: " + url);

            // Send the "read" to the client
            CompletableFuture.runAsync(() -> {
                try {
                    // Create secureDTO that will be sent to respective servers
                    byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
                    SecureDTO secureDTO = CryptoService.serverGenerateNewSecureDTO(report, s, randomBytes, ServerApplication.serverId + "");
                    secureDTO.setTimestamp(timestamp.get());
                    CryptoService.signSecureDTO(secureDTO, CryptoUtils.getServerPrivateKey(ServerApplication.serverId));

                    // Send the message to the server, this endpoint won't return anything
                    // so its pointless to wait for response.
                    sendMessageToClient(secureDTO, url);
                    System.out.println("[Server Id: " + ServerApplication.serverId + "] Spontaneous read sent successfully.");
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        System.out.println("[Server Id: " + ServerApplication.serverId + "] Byzantine Atomic Register WRITE, sending ACK to clientId: " + userId);

        // Sends an ACKNOWLEDGE to the writer
        return new AcknowledgeDto(timestamp.get(), ServerApplication.serverId);
    }

    /**
     *  Used when obtainLocationReport is called, to read atomically the last written report.
     *
     *  When an request is received, the process will be registered to the listeners list so they can be
     *  spontaneously alerted of a concurrent write (submitLocationReport).
     */
    public static ReportDTO receiveReadRequest(RequestLocationDTO req, int rid, UserService userService) throws ApplicationException {
        listening.putIfAbsent(req.getUserIDSender(), rid);

        return userService.obtainLocationReport(req.getUserIDSender(), req.getUserIDRequested(), req.getEpoch());
    }

    /**
     * Received a read complete, unregister the client
     * with the same RID from the listeners.
     *
     */
    public static void readCompleteReceived(ReadCompleteDTO readCompleteDTO) {
        listening.remove(readCompleteDTO.getClientId());
    }

    /**
     *  Sends HTTP request
     */
    private static SecureDTO sendMessageToClient(SecureDTO message, String url) throws UnreachableClientException {
        try {
            // Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            // Send request and return the SecureDTO with the ReportDTO encapsulated
            HttpEntity<SecureDTO> entity = new HttpEntity<>(message, headers);
            ResponseEntity<SecureDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, SecureDTO.class);
            return result.getBody();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new UnreachableClientException("[Server " + ServerApplication.serverId + "] Byzantine Atomic register - Wasn't able to contact server.");
        }
    }
}
