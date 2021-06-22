package pt.ulisboa.tecnico.sec.secureclient.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import pt.ulisboa.tecnico.sec.secureclient.SpecialClientApplication;
import pt.ulisboa.tecnico.sec.services.configs.ByzantineConfigurations;
import pt.ulisboa.tecnico.sec.services.configs.PathConfiguration;
import pt.ulisboa.tecnico.sec.services.dto.ClientResponseDTO;
import pt.ulisboa.tecnico.sec.services.dto.RequestProofDTO;
import pt.ulisboa.tecnico.sec.services.dto.SecureDTO;
import pt.ulisboa.tecnico.sec.services.exceptions.UnreachableClientException;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoService;
import pt.ulisboa.tecnico.sec.services.utils.crypto.CryptoUtils;

public class NetworkService {
	
	private static RestTemplate restTemplate = new RestTemplate();
	
	private NetworkService() {}
	
	public static SecureDTO sendMessageToServers(ArrayList<SecureDTO> messages, String endpoint) {
        // Used to block the requesting thread until all asynchronous requests complete
        CountDownLatch latch = new CountDownLatch(ByzantineConfigurations.NUMBER_OF_SERVERS);

        // Messages received from servers
        List<SecureDTO> readlist = Collections.synchronizedList(new ArrayList<SecureDTO>());

        for (int i = 1; i <= ByzantineConfigurations.NUMBER_OF_SERVERS; i++) {
            int serverId = i;
            SecureDTO secureDTO = messages.get(i-1);
            CompletableFuture.runAsync(() -> {
                try {
                    // Build the URL that the request will be sent
                    String url = PathConfiguration.buildUrl(PathConfiguration.getServerUrl(serverId), endpoint);

                    // Send the message to the server & receive answer asynchronously
                    SecureDTO sec = sendMessageToServer(secureDTO, url);

                    if (sec == null) {
                        System.out.println("[Client " + SpecialClientApplication.userId + "] Wasn't able to contact server " + serverId);
                    } else {
                        if (secureDTO != null && CryptoService.checkSecureDTODigitalSignature(sec, CryptoUtils.getServerPublicKey(serverId + ""))) {
                            System.out.println("[Client " + SpecialClientApplication.userId + "] Byzantine regular register received secureDTO");
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

        return readlist.get(0);
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
            throw new UnreachableClientException("[Client " + SpecialClientApplication.userId + "] Byzantine Regular register - Wasn't able to contact server.");
        }
    }


    /**
     *  ???
     */
    public static ClientResponseDTO sendMessageToClient(RequestProofDTO request, String url) {
        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        // Send request and return the SecureDTO with the ReportDTO encapsulated
        HttpEntity<RequestProofDTO> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ClientResponseDTO> result = restTemplate.exchange(url, HttpMethod.POST, entity, ClientResponseDTO.class);
        return result.getBody();
    }
}
