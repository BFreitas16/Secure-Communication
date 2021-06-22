package pt.ulisboa.tecnico.sec.secureclient.services;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.UnreachableClientException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service used by the operations: obtainUsersAtLocation & requestMyProofs
 */
public class ByzantineRegularRegisterService {
	
	private ByzantineRegularRegisterService() {}
	
    // Send HTTP requests
    private static RestTemplate restTemplate = new RestTemplate();

    // Request ID for the current read, the server must return the same r = rid
    private static AtomicInteger rid = new AtomicInteger(0);


    /**
     * Called when the client wants to read
     * something from the server.
     *
     * It will send a request with the same RID
     * to all servers.
     *
     * Then it has to wait for the answer and verify the digital signature
     * if its valid then add to the readlist until there are > (N+f)/2 messages.
     * Pick the value with the highest timestamp and return that to the user.
     */
    @SuppressWarnings("unchecked")
	public static synchronized <P, R> R readFromRegisters(P unsecureDTO, Class<R> responseClass, String userIdSender, String endpoint) throws ApplicationException {
        // Used to block the requesting thread until all asynchronous requests complete
        CountDownLatch latch = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);

        // Messages received from servers
        List<SecureDTO> readlist = Collections.synchronizedList(new ArrayList<SecureDTO>());

        SecureDTO response = null;

        // Build the secureDtos
        byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
        ArrayList<SecureDTO> secureDTOS = buildSecureDtosForAllServers(unsecureDTO, userIdSender, randomBytes, rid.incrementAndGet());

        // Send to each server a read request with the current RID
        for (int i = 1; i <= ClientApplication.numberOfServers; i++) {
            int serverId = i;

            // Get secureDto
            SecureDTO secureDTO = secureDTOS.get(i-1);

            CompletableFuture.runAsync(() -> {
                try {
                    // Build the URL that the request will be sent
                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), endpoint);

                    // Send the message to the server & receive answer asynchronously
                    SecureDTO sec = sendMessageToServer(secureDTO, url);

                    if (sec == null) {
                        System.out.println("[Client " + ClientApplication.userId + "] Wasn't able to contact server " + serverId);
                    } else {
                        if (secureDTO != null && CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId + ""))) {
                            System.out.println("[Client " + ClientApplication.userId + "] Byzantine regular register received secureDTO");
                            readlist.add(sec);
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
        if(readlist.size() > (ClientApplication.numberOfServers + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
            System.out.println("[Client " + ClientApplication.userId + "] Byzantine regular register obtained minimum quorum.");

            // From the replies, choose the one with highest timestamp and return it
            SecureDTO highestSecureDto = highestval(readlist);
            R unwrappedDTO = (R) CryptoService.extractEncryptedData(highestSecureDto, responseClass, CryptoUtils.createSharedKeyFromString(randomBytes));
            return unwrappedDTO;
        }

        // A byzantine quorum minimum wasn't met.
        throw new ApplicationException("Client " + ClientApplication.userId + " wasn't able to obtain at least (N+f)/2 responses.");
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
            throw new UnreachableClientException("[Client " + ClientApplication.userId + "] Byzantine Regular register - Wasn't able to contact server.");
        }
    }

    /**
     * From a list of secure dto's, returns the one
     * with highest timestamp.
     */
    private static SecureDTO highestval(List<SecureDTO> readlist) {
        SecureDTO highestSecureDto = readlist.get(0);
        long highestTimestamp = highestSecureDto.getTimestamp();

        for (SecureDTO secureDTO : readlist) {
            long currTimestamp = secureDTO.getTimestamp();
            if(currTimestamp > highestTimestamp)
                highestSecureDto = secureDTO;
        }

        return highestSecureDto;
    }


    /**
     *  Builds a list of secure DTOs that will be sent to the server
     */
    private static <R> ArrayList<SecureDTO> buildSecureDtosForAllServers(R req, String userIdSender, byte[] randomBytes, int rid) throws ApplicationException {
        ArrayList<SecureDTO> secureDTOS = new ArrayList<>();
        for (int serverId = 1; serverId <= ByzantineConfigurations.NUMBER_OF_SERVERS; serverId++) {
            // Create secureDTO that will be sent to respective servers
            SecureDTO secureDTO = CryptoService.generateNewSecureDTO(req, userIdSender, randomBytes, serverId + "");

            // Set RID for read requests, not needed for WRITE requests
            if(rid != -1)
                secureDTO.setRid(rid);

            // Build the proof of work
            //TODO: Fazer apenas um proof of work
            secureDTO.setProofOfWork(ProofOfWorkService.findSolution(secureDTO.getData()));

            // Sign the DTO
            CryptoService.signSecureDTO(secureDTO, CryptoUtils.getClientPrivateKey(ClientApplication.userId));

            secureDTOS.add(secureDTO);
        }

        return secureDTOS;
    }
}
