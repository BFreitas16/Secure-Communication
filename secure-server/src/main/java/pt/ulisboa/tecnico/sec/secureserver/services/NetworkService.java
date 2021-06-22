package pt.ulisboa.tecnico.sec.secureserver.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.sec.secureserver.ServerApplication;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.RequestDTO;
import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NetworkService {

    // HTTP messages template
    private static RestTemplate restTemplate = new RestTemplate();

    // Locks used to block the current requesting thread, until the broadcast algorithm completes.
    private static Map<String, Object> deliverWait = new ConcurrentHashMap<>();

    // Map of received ECHOS from other servers, key: clientId of original request. The value Map has as key the serverId and the original request
    private static Map<String, Map<String, RequestDTO>> echos = new ConcurrentHashMap<>();

    // Map of received READIES from other servers, key: clientId of original request. The value Map has as key the serverId and the original request
    private static Map<String, Map<String, RequestDTO>> readies = new ConcurrentHashMap<>();

    // Map of SENT ECHO messages received by clientId
    private static Map<String, Boolean> sentEcho = new ConcurrentHashMap<>();

    // Map of SENT READY messages received by clientId
    private static Map<String, Boolean> sentReady = new ConcurrentHashMap<>();

    // Map of DELIVERED messages by clientId
    private static Map<String, Boolean> delivered = new ConcurrentHashMap<>();

    // Broadcast protocol worker threads
    private static Thread echoJob;
    private static Thread readyJob;

    /**
     * Called when the application starts.
     */
    public static void init() {
        waitForQuorum();
    }

    /**
     * Creates necessary threads to listen and count
     * the received "ECHO" and "READY" messages.
     */
    public static void waitForQuorum() {
        //Thread waiting for quorum of echos
        echoJob = new Thread(NetworkService::echoJobWork);

        //Waiting for quorum of readys
        readyJob = new Thread(NetworkService::readyJobWork);

        // Start threads
        echoJob.start();
        readyJob.start();
    }

    /**
     * Code executed by the thread "echoJob". Only one thread should be executing
     * this code.
     *
     * It will for each client request, check if the number of echoes received from
     * other servers is more than (N+f)/2, and if so, send a "READY" message with that
     * associated request to all other servers.
     */
    private static void echoJobWork() {
        while (true) {
            //Count echos received from other servers, to ALL "clientId" entries in "echos" map
            for (Map.Entry<String, Map<String, RequestDTO>> clientMap : echos.entrySet()) {

                // Requests associated with a certain clientId request, echoed by other servers
                final Collection<RequestDTO> requests = clientMap.getValue().values();
                Set<RequestDTO> uniqueSet = new HashSet<>(requests);

                // Different requests made by the same clientId
                for (RequestDTO temp : uniqueSet) {
                    if (temp == null) {
                        continue;
                    }

                    // Number of times that this request was "ECHOED" by other servers
                    int numRequestEchoed = Collections.frequency(requests, temp);
                    // If the request was "ECHOED" more than (N+f)/2, then current server sends a ready message to all other servers (STEP 5 protocol)
                    if ( numRequestEchoed > (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2
                            && !sentReady.containsKey(clientMap.getKey())) {

                        sentReady.putIfAbsent(clientMap.getKey(), true);
                        temp.setServerId(ServerApplication.serverId);
                        sendReadyOrEcho(temp, PathConfiguration.SERVER_READY);
                    }
                }
            }

            // Sleep some time before running the protocol again
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }

    }

    /**
     * Code executed by the thread "readyJob". It counts the number of "READY" messages
     * received from other servers.
     *
     * If it is > f and sentReady = false, then it will sentReady = true
     * and send "READY" messages to other servers.
     *
     * If sentReady = faulty byzantine processes * 2, then deliver the message.
     */
    private static void readyJobWork() {
        while (true) {
            // For each of the clientId requests in the ready map
            for (Map.Entry<String, Map<String, RequestDTO>> clientMap : readies.entrySet()) {
                final Collection<RequestDTO> requests = clientMap.getValue().values();
                Set<RequestDTO> uniqueSet = new HashSet<>(requests);

                // Check if the specific request satisfies the conditions to be DELIVERED
                for (RequestDTO temp : uniqueSet) {
                    if (temp == null) {
                        continue;
                    }

                    // Number of "READY" messages sent by other servers to the current request
                    int numOfReady = Collections.frequency(requests, temp);
                    // If we received more than f ( number of faulty processess) "READY" messages without sending a READY message
                    // it means we missed a couple "ECHO" messages. Change the state to sentReady
                    // and send the READIES to other servers.
                    // This step is crucial to guarantee the "TOTALITY" property.
                    if ( numOfReady > ByzantineConfigurations.MAX_BYZANTINE_FAULTS
                            && !sentReady.containsKey(clientMap.getKey())) {
                        sentReady.putIfAbsent(clientMap.getKey(), true);
                        temp.setServerId(ServerApplication.serverId);
                        sendReadyOrEcho(temp, PathConfiguration.SERVER_READY);
                    }

                    // If the server received enough "READY" messages from other servers, then deliver message
                    if ( numOfReady > 2 * ByzantineConfigurations.MAX_BYZANTINE_FAULTS
                            && !delivered.containsKey(clientMap.getKey())) {
                        delivered.putIfAbsent(clientMap.getKey(), true);

                        //Instruct requests that are waiting for the protocol to complete, to wake up
                        // and continue working.
                        Object deliveryLock = deliverWait.get(clientMap.getKey());
                        if (deliveryLock == null){
                            continue;
                        }
                        synchronized (deliveryLock) {
                            deliveryLock.notifyAll();
                        }
                        deliverWait.remove(clientMap.getKey());
                    }
                }
            }

            // Sleep some time before executing the protocol again
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     *   Executed by the CONTROLLER (endpoints) after receiving a request from a client or HA user.
     *
     *   1º It initializes the data structures associated with the clientId of the sender of the request.
     *
     *   2º Sends "ECHO" messages to all other servers with the received REQUEST.
     *
     *   3º It then waits for the protocol to complete by locking on a object of the deliverWait map.
     *
     *   4º After the blocked thread wakes up, times out or error happens it will clean the associated data
     *   structures and check if the message was delivered by checking the "delivered" map.
     */
    public static void sendBroadcast(RequestDTO request) throws ApplicationException {
        String clientId = request.getClientId();

        // Restart client data structures
        initClient(clientId);

        // Adds a lock for the current clientId to signal that its waiting for the protocol to end
        // The clientId by itself is not enough in case a client sent 2 requests or more they would wait
        // under the same lock because of the method "putIfAbsent" and as soon one request finished the others
        // would also finish and fail.
        //TODO: Arranjar solução para o problema de multiplos pedidos
        String lockName = clientId;
        deliverWait.putIfAbsent(lockName, new Object());

        // Sends 'ECHO' messages of the received REQUEST to all other servers
        if (!sentEcho.containsKey(clientId)) { //if sentecho = false
            sentEcho.put(clientId, true);
            sendReadyOrEcho(request, PathConfiguration.SERVER_ECHO);
        }

        // If the current clientId is not on the delivered map then wait until the broadcast completes,
        // error happens or thread times out.
        if (!delivered.containsKey(clientId)) {
                try {
                    // If it times out, there is still the chance that the request completed
                    //TODO Problems with time server 4 completes the write request but the others fail o obtain a byz quorum
                    Object deliveryLock = deliverWait.get(lockName);
                    synchronized (deliveryLock) {
                        deliveryLock.wait(20000);
                    }
                } catch (InterruptedException e) {
                    throw new ApplicationException("Thread Interrupted while waiting on deliverWait lock for request of clientId: " + clientId, e);
                }
        }

        // Removes the "ECHO" and "READY" messages received from other servers, associated with this request
        cleanClient(clientId);

        // If the clientId is not on the delivered map then the protocol failed.
        if (!delivered.containsKey(clientId)) {
            throw new ApplicationException("Broadcast was unable to met a quorum");
        }
    }

    /**
     *  Initializes the data structures of the "Double Echo Broadcast" that are
     *  related with clientId.
     */
    private static void initClient(String clientId) {
        sentEcho.remove(clientId);
        sentReady.remove(clientId);
        delivered.remove(clientId);
    }

    /**
     *  Removes "ECHO" and "READY" messages related with clientId.
     */
    private static void cleanClient(String clientId) {
        echos.remove(clientId);
        readies.remove(clientId);
    }

    /**
     *  Delivers "ECHO" messages received from other servers.
     */
    public void echo(RequestDTO request) {
        String clientId = request.getClientId();
        String serverId = request.getServerId();

        // Creates a new entry for the client id if there is not one already
        echos.putIfAbsent(clientId, new ConcurrentHashMap<>());

        // Adds a echo message related with "clientId" and received from server with "serverId"
        echos.get(clientId).put(serverId, request);
    }

    /**
     *  Delivers "READY" messages received from other servers
     */
    public void ready(RequestDTO request) {
        String clientId = request.getClientId();
        String serverId = request.getServerId();

        // Creates a new entry related with clientId
        readies.putIfAbsent(request.getClientId(), new ConcurrentHashMap<>());

        // Adds a ready message received from server with serverId
        readies.get(clientId).put(serverId, request);
    }

    /**
     *   Send "READY" or "ECHO" messages to all servers with the associated
     *   request made by the user, to achive the same status
     *   across all machines.
     */
    static void sendReadyOrEcho(RequestDTO request, String path) {
        for (int serverId = 1; serverId<= ServerApplication.numberOfServers; serverId++) {

            // Convert the above request body to a secure request object
            byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
            SecureDTO secureDTO = CryptoService.generateNewSecureDTO(request, ServerApplication.serverId, randomBytes, serverId + "");
            CryptoService.signSecureDTO(secureDTO, CryptoUtils.getServerPrivateKey(ServerApplication.serverId));

            String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), path + ServerApplication.serverId);
            sendMessageToServer(secureDTO, url);
        }
    }

    /**
     *  Sends an HTTP message and synchronously waits for the response.
     */
    private static SecureDTO sendMessageToServer(SecureDTO message, String url) {
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        // Send request and return the SecureDTO with the ReportDTO encapsulated
        HttpEntity<SecureDTO> entity = new HttpEntity<>(message, headers);
        ResponseEntity<SecureDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, SecureDTO.class);
        return result.getBody();
    }

}
