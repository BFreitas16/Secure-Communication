package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.UnreachableClientException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service used by the operations: obtainLocationReport & submitLocationReport
 * As the obtainLocationReport should return the same information on all
 * correct server processes if it was delivered by a correct server process.
 */
public class ByzantineAtomicRegisterService {
	
	private ByzantineAtomicRegisterService() {}

    // Used to send HTTP requests
    private static RestTemplate restTemplate = new RestTemplate();

    // Timestamp used in the write request
    private static AtomicLong timestamp = new AtomicLong(0);

    // RID, used to send read requests
    private static AtomicInteger rid = new AtomicInteger(0);

    // Answers received during the reading request, a byzantine quorum of (N+f) / 2 needs to be met
    // this structure can receive spontaneous READs from servers when there is a concurrent write.
    // The key is the timestamp and value is another hashmap where the key is the serverId and value the reportDTO.
    private static ConcurrentHashMap<Long, ConcurrentHashMap<String, ReportDTO>> answers = new ConcurrentHashMap<>();

    /**
     *  Starts an atomic write to the server registers.
     *
     *  The write should successfully return if the acklist count is
     *  higher than (N + f) / 2.
     *
     *  The entry to the method is synchronized
     */
    public static synchronized void writeToRegisters(ReportDTO report, String userIdSender) throws ApplicationException {
        // Used to block the requesting thread until all asynchronous requests complete
        CountDownLatch latch = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);

        // Maps serverId to acknowledge during the write phase of the protocol
        ConcurrentHashMap<String, AcknowledgeDto> acklist = new ConcurrentHashMap<String, AcknowledgeDto>();

        // Timestamp for the current write
        long currTimestamp = timestamp.incrementAndGet();

        // Build secure dtos
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        ArrayList<SecureDTO> secureDTOS = buildSecureDtosForAllServers(report, userIdSender, randomBytes, -1, currTimestamp);

        for (int i = 1; i <= ByzantineConfigurations.NUMBER_OF_SERVERS; i++) {
            int serverId = i;

            SecureDTO secureDTO = secureDTOS.get(i-1);

            CompletableFuture.runAsync(() -> {
                try {

                    // Build the URL that the request will be sent
                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), PathConfiguration.SUBMIT_REPORT_ENDPOINT);

                    // Send the message to the server & receive answer asynchronously
                    // Response (SecureDto has an AcknowledgeDTO encapsulated)
                    SecureDTO sec = sendMessageToServer(secureDTO, url);

                    // Check if it received any response
                    if (sec == null) {
                        System.out.println("[Client " + SpecialClientApplication.userId + "] WRITE Byzantine Atomic register - Wasn't able to contact server " + serverId);
                    } else {
                        // Check if the received response has a valid digital signature
                        if (CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId + ""))) {
                            System.out.println("[Client " + SpecialClientApplication.userId + "] WRITE Byzantine Atomic register - received secureDTO");

                            // Adds the received acknowledge to acklist so the quorum can be checked
                            AcknowledgeDto ackDto = (AcknowledgeDto) CryptoService.extractEncryptedData(sec, AcknowledgeDto.class, CryptoUtils.createSharedKeyFromString(randomBytes));

                            // When application exceptions are thrown by the server e.g. "report duplicated" or "report not found"
                            // the server won't answer an ACKNOWLEDGE DTO but an ErrorMessageResponse, which will then fail the conversion
                            // in the function extractEncryptedData above. Then this client request will throw an exception saying
                            // that the byzantine quorum wasn't meet, which is false...
                            if(ackDto == null)
                                acklist.put(serverId+"", new AcknowledgeDto());
                            else
                                acklist.put(ackDto.getServerId(), ackDto);
                        }
                    }
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }

                // Decrement the latch after this asynchronous thread completes its work, so the main requester
                // can keep working.
                latch.countDown();
            });
        }

        // Block requesting thread until all HTTP requests are answered
        boolean responses;
        do {
            try {
                responses = latch.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                responses = true;
            }
        } while (!responses);


        // If the number of replies is bigger than (N+f)/2, the byzantine quorum is met and 1 reply is correct
        if(acklist.size() > (SpecialClientApplication.numberOfServers + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
            System.out.println("[Client " + SpecialClientApplication.userId + "] Write Byzantine Atomic register obtained minimum quorum to execute the write request!");

            //Everything went well, clean data structures and return
            acklist.clear();
            return;
        }

        // A byzantine quorum minimum wasn't met.
        throw new ApplicationException("Client " + SpecialClientApplication.userId + " wasn't able to obtain at least (N+f)/2 responses for the WRITE atomic operation, some servers may be in an inconsistent status.");
    }


    /**
     *  Starts an atomic read to the server registers.
     *
     *  The number of identical answers should be higher than (N+f)/2 to
     *  successfully return.
     */
    public static synchronized ReportDTO readFromRegisters(RequestLocationDTO req) throws ApplicationException {
        // Used to block the requesting thread until all asynchronous requests complete
        CountDownLatch latch = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);

        // Obtain current read id and increment it, it will be used to identify current request
        int currRid = rid.incrementAndGet();

        // Clear all answers entries for all timestamps
        answers.clear();

        //Secure dtos to be sent
        // Send READ request to all servers
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        ArrayList<SecureDTO> secDtos = buildSecureDtosForAllServers(req, req.getUserIDSender(), randomBytes, currRid, -1);

        for (int i = 1; i <= ByzantineConfigurations.NUMBER_OF_SERVERS; i++) {
            int serverId = i;

            // Copy to be able to use it locally in the lambda of the async task
            byte[] finalRandomBytes = randomBytes;

            // The secure DTO being sent
            SecureDTO secureDTO = secDtos.get(serverId-1);

            CompletableFuture.runAsync(() -> {
                try {
                    // Build the URL that the request will be sent
                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), PathConfiguration.GET_REPORT_ENDPOINT);

                    // Send the message to the server & receive answer asynchronously
                    // Response (SecureDto has an ReportDTO encapsulated)
                    SecureDTO sec = sendMessageToServer(secureDTO, url);

                    // Check if it received any response
                    if (sec == null) {
                        System.out.println("[Client " + SpecialClientApplication.userId + "] READ Byzantine Atomic register - Wasn't able to contact server " + serverId);
                    } else {
                        // Check if the received response has a valid digital signature
                        if (CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId + ""))) {
                            System.out.println("[Client " + SpecialClientApplication.userId + "] READ Byzantine Atomic register - received secureDTO");

                            // Adds the received acknowledge to acklist so the quorum can be checked
                            ReportDTO report = (ReportDTO) CryptoService.extractEncryptedData(sec, ReportDTO.class, CryptoUtils.createSharedKeyFromString(finalRandomBytes));

                            // Creates an entry for the current timestamp if there is not  one already
                            answers.putIfAbsent(sec.getTimestamp(), new ConcurrentHashMap<>());

                            // Adds to the received timestamp, an entry for a ReportDTO returned by the serverId
                            answers.get(sec.getTimestamp()).putIfAbsent(String.valueOf(serverId), report);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                // Decrement the latch after this asynchronous thread completes its work, so the main requester
                // can keep working.
                latch.countDown();
            });
        }

        // Block requesting thread until all HTTP requests are answered
        boolean responses;
        do {
            try {
                responses = latch.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                responses = true;
            }
        } while (!responses);


        // If the read value from the server is equal to the current rid
        // check for each timestamp entry, if there exists a value "v"
        // which its count is higher than (N+f)/2, if there is send a READCOMPLETE message
        // to all servers to unregister itself from the listening list.
        randomBytes = CryptoUtils.generateRandom32Bytes();

        // Send READCOMPLETE to all servers
        ReadCompleteDTO readCompleteDTO = new ReadCompleteDTO();
        readCompleteDTO.setRid(currRid);
        readCompleteDTO.setClientId(SpecialClientApplication.userId);

        secDtos = buildSecureDtosForAllServers(readCompleteDTO, req.getUserIDSender(), randomBytes, currRid, -1);

        ArrayList<ReportDTO> reports = flattenHashMaps();
        for (ReportDTO report : reports) {
            int occurrences = Collections.frequency(reports, report);
            if(occurrences > (SpecialClientApplication.numberOfServers + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
                answers.clear();
                System.out.println("[Client id:" + SpecialClientApplication.userId+"] Obtained byzantine quorum for the atomic READ operation!");


                for (int i = 1; i <= ByzantineConfigurations.NUMBER_OF_SERVERS; i++) {
                    int serverId = i;

                    // Create secureDTO that will be sent to respective servers
                    SecureDTO secureDTO = secDtos.get(serverId-1);

                    CompletableFuture.runAsync(() -> {
                        try {
                            // Build the URL that the request will be sent
                            String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), PathConfiguration.READ_COMPLETE_ENDPOINT);

                            // Send the message to the server
                            sendMessageToServer(secureDTO, url);
                            System.out.println("[Client id:" + SpecialClientApplication.userId+"] READ Byzantine Atomic register - Sent READCOMPLETE message to serverId: " + serverId);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    });
                }

                return report;
            }
        }

        // A byzantine quorum minimum wasn't met.
        throw new ApplicationException("Client " + SpecialClientApplication.userId + " wasn't able to obtain at least (N+f)/2 responses for the READ atomic operation, some servers may be in an inconsistent status.");
    }

    /**
     *  It returns all reports dto's across all timestamps
     *  of the answers hashmap.
     *  TODO: Probably this will not work lul keep an eye on it
     */
    private static ArrayList<ReportDTO> flattenHashMaps() {
        ArrayList<ReportDTO> reports = new ArrayList<>();
        for (Long key : answers.keySet()) {
            ConcurrentHashMap<String, ReportDTO> curr = answers.get(key);
            reports.addAll(curr.values());
        }

        return reports;
    }

    /**
     * A spontaneous read is triggered by the server and
     * described in the step 3 of the "Byzantine Quorum with Listeners (part 1, write)".
     * This is used to alert all registered clients of concurrent writes.
     */
    public static void receiveSpontaneousRead(ReportDTO report, long timestamp, String serverId) {
        // Insert answers by timestamp and with value an entry <ServerId, ReportDTO>
        answers.putIfAbsent(timestamp, new ConcurrentHashMap<>());
        answers.get(timestamp).put(serverId, report);
    }

    /**
     *  Builds a list of secure DTOs that will be sent to the server
     */
    private static <R> ArrayList<SecureDTO> buildSecureDtosForAllServers(R req, String userIdSender, byte[] randomBytes, int rid, long timestamp) throws ApplicationException {
        ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
        for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
            // Create secureDTO that will be sent to respective servers
            SecureDTO secureDTO = CryptoService.generateNewSecureDTO(req, userIdSender, randomBytes, serverId + "");

            // Set timestamp
            if(timestamp != -1)
                secureDTO.setTimestamp(timestamp);

            // Set RID for read requests, not needed for WRITE requests
            if(rid != -1)
                secureDTO.setRid(rid);

            // Build the proof of work
            //TODO: Fazer apenas um proof of work
            secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

            // Sign the DTO
            CryptoService.signSecureDTO(secureDTO, CryptoUtils.getClientPrivateKey(userIdSender));
            secureDTOS.add(secureDTO);
        }

        return secureDTOS;
    }


    /**
     *  Sends HTTP request
     */
    private static SecureDTO sendMessageToServer(SecureDTO message, String url) throws UnreachableClientException {
        try {
            // Set HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            // Send request and return the SecureDTO with the ReportDTO encapsulated
            HttpEntity<SecureDTO> entity = new HttpEntity<>(message, headers);
            ResponseEntity<SecureDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, SecureDTO.class);
            return result.getBody();
        } catch (Exception e) {
            throw new UnreachableClientException("[Client " + SpecialClientApplication.userId + "] Byzantine Atomic register - Wasn't able to contact server.");
        }
    }
}
