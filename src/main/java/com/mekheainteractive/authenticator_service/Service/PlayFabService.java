package com.mekheainteractive.authenticator_service.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
public class PlayFabService {

    @Value("${playfab.title-id}")
    private String titleId;

    @Value("${playfab.secret-key}")
    private String secretKey;

    private WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://" + titleId + ".playfabapi.com/Server")
                .defaultHeader("X-SecretKey", secretKey)
                .build();
    }

    // Verify PlayFab session ticket
    public String verifySessionTicket(String sessionTicket) {
        try {
            Map<String, Object> requestBody = Map.of("SessionTicket", sessionTicket);

            Map response = webClient()
                    .post()
                    .uri("/AuthenticateSessionTicket")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // blocking call for simplicity; you can also make it async

            if (response == null) {
                System.err.println("PlayFab response is null");
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                System.err.println("PlayFab response missing 'data'");
                return null;
            }

            Map<String, Object> userInfo = (Map<String, Object>) data.get("UserInfo");
            if (userInfo == null) {
                System.err.println("PlayFab UserInfo is null");
                return null;
            }

            String playFabId = (String) userInfo.get("PlayFabId");
            System.out.println("Extracted PlayFabId: " + playFabId);
            return playFabId;

        } catch (WebClientResponseException ex) {
            // This gives the raw response from PlayFab (could be JSON or HTML error page)
            System.err.println("PlayFab verify session error: " + ex.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}