package pt.ulisboa.tecnico.sec.secureclient.services;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import pt.ulisboa.tecnico.sec.secureclient.ClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.*;
import pt.ulisboa.tecnico.sec.services.exceptions.ApplicationException;
import pt.ulisboa.tecnico.sec.services.exceptions.UnreachableClientException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

public class NetworkService {
	
	private static RestTemplate restTemplate = new RestTemplate();
	private static CountDownLatch latch;
	private static Map<Integer, Map<String, ReportDTO>> answersReport = new ConcurrentHashMap<>();
    private static Map<Integer, Map<String, ResponseUserProofsDTO>> answersUserProofs = new ConcurrentHashMap<>();
	private static int readId = 0;
	private static ReportDTO lastReportAnswers; //TODO: N garantimos que todos clientes lêm mesma cena
	private static ResponseUserProofsDTO lastUserProofsAnswers;

	private NetworkService() {}
	
	@SuppressWarnings("unchecked")
	public static <P, R> R sendMessageToServers(P unsecureDTO, Class<R> responseClass, String userIdSender, String endpoint) {
        ConcurrentLinkedDeque<R> replies = new ConcurrentLinkedDeque<>();
        CountDownLatch latchResponses = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);
        AtomicInteger successfulRequest = new AtomicInteger();
        latch = new CountDownLatch(1);
        //Clear
        clearAnswers(responseClass);
        readId++;

        for (int i=1; i<= ClientApplication.numberOfServers; i++) {
            int serverId = i;
            CompletableFuture.runAsync(() -> {
                try {
                    // Convert the above request body to a secure request object
                    byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
                    SecureDTO secureDTO = CryptoService.generateNewSecureDTO(unsecureDTO, userIdSender, randomBytes, serverId + "");

                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), endpoint);

                    SecureDTO sec = sendMessageToServer(secureDTO, url);

                    if (sec == null) {
                        System.out.println("[Client " + ClientApplication.userId + "] Wasn't able to contact server " + serverId);
                    }
                    else {
                        R unwrappedDTO = (R) CryptoService.extractEncryptedData(sec, responseClass, CryptoUtils.createSharedKeyFromString(randomBytes));

                        // Verify if conversion was successful and its a response dto
                        if (unwrappedDTO != null && CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId + "")))
                            replies.add(unwrappedDTO);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                latchResponses.countDown();
            }).exceptionally(e -> null);
        }


        boolean numberOfResponses = false;
        R res;
        do {
            try {
                numberOfResponses = latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                numberOfResponses = true;
            }
            res = existsQuorumOfResponses(responseClass);
        } while (!numberOfResponses && res != null);

        if (successfulRequest.get() <= (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
            completeGetDTO(userIdSender, readId);
            return null;
        }

        if (res != null) {
            completeGetDTO(userIdSender, readId);
            return res;
        }

        completeGetDTO(userIdSender, readId);
        System.out.println(readId);
        return getLastAnswer(responseClass);
	}

	private static <R> void clearAnswers(Class<R> classType) {
	    if (classType.equals(ReportDTO.class)){
            answersReport.clear();
        } else if (classType.equals(ResponseUserProofsDTO.class)){
            answersUserProofs.clear();
        }
    }

    private static void completeGetDTO(String userId, int readId) {
        for (int i=1; i<= ClientApplication.numberOfServers; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    //TODO SEND READ COMPLETE
                   // serviceEntry.getValue().completeGetStateOfGood(user.getUserId(), goodId);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
	private static <R> R getLastAnswer(Class<R> classType) {
        if (classType.equals(ReportDTO.class)){
            return (R) lastReportAnswers;
        } else if (classType.equals(ResponseUserProofsDTO.class)){
            return (R) lastUserProofsAnswers;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
	private static <R> R existsQuorumOfResponses(Class<R> classType) {
        if (classType.equals(ReportDTO.class)){
            for (Map.Entry<Integer, Map<String, ReportDTO>> entry : answersReport.entrySet()) {
                final Collection<ReportDTO> report = entry.getValue().values();
                Set<ReportDTO> uniqueSet = new HashSet<>(report);
                for (ReportDTO temp : uniqueSet) {
                    if (temp == null) {
                        continue;
                    }
                    if (Collections.frequency(report, temp) > (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
                        return (R) temp;
                    }
                }
            }
            return null;

        } else if (classType.equals(ResponseUserProofsDTO.class)){
            for (Map.Entry<Integer, Map<String, ResponseUserProofsDTO>> entry : answersUserProofs.entrySet()) {
                final Collection<ResponseUserProofsDTO> responseUserProofs = entry.getValue().values();
                Set<ResponseUserProofsDTO> uniqueSet = new HashSet<>(responseUserProofs);
                for (ResponseUserProofsDTO temp : uniqueSet) {
                    if (temp == null) {
                        continue;
                    }
                    if (Collections.frequency(responseUserProofs, temp) > (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2) {
                        return (R) temp;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public static <P> void sendMessageToServersWithoutResponse(P unsecureDTO, String userIdSender, String endpoint) throws ApplicationException {
        ConcurrentLinkedDeque<SecureDTO> returnValue = new ConcurrentLinkedDeque<>();
        CountDownLatch latch = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);
		// Convert the above request body to a secure request object

        for (int i=1; i<= ClientApplication.numberOfServers; i++){
            int serverId = i;
            CompletableFuture.runAsync(() -> {
                try {
                    byte[] randomBytes = CryptoUtils.generateRandom32Bytes();
                    SecureDTO secureDTO = CryptoService.generateNewSecureDTO(unsecureDTO, userIdSender, randomBytes, serverId + "");
                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), endpoint);

                    try {
                        SecureDTO sec = sendMessageToServer(secureDTO, url);
                        if (sec == null) {
                            System.out.println("[Client " + ClientApplication.userId + "] Wasn't able to contact server " + serverId);
                        }else {
                            String answer = (String) CryptoService.extractEncryptedData(sec, String.class, CryptoUtils.createSharedKeyFromString(randomBytes));
                            System.out.println(answer);
                            returnValue.add(sec); //TODO If sec has exception
                        }
                    } catch(Exception e) {
                        throw new UnreachableClientException("[Client "+ ClientApplication.userId+"] Wasn't able to contact server.");
                    }
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                latch.countDown();
            });
        }

        boolean responses;
        do {
            try {
                responses = latch.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                responses = true;
            }
        } while (!responses && returnValue.size() <= (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2);

        if (returnValue.size() <= (ByzantineConfigurations.NUMBER_OF_SERVERS + ByzantineConfigurations.MAX_BYZANTINE_FAULTS) / 2){
            System.out.println("Report was not submitted. Not enough responses from Servers.");
        } else {
            System.out.println("report submitted");
        }
	}

	private static SecureDTO sendMessageToServer(SecureDTO message, String url) {
		// Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        // Send request and return the SecureDTO with the ReportDTO encapsulated
        HttpEntity<SecureDTO> entity = new HttpEntity<>(message, headers);
        ResponseEntity<SecureDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, SecureDTO.class);
        return result.getBody();
	}
	
	public static ClientResponseDTO sendMessageToClient(RequestProofDTO request, String url) {
		// Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        HttpEntity<RequestProofDTO> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ClientResponseDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, ClientResponseDTO.class);
        return result.getBody();
	}

}
