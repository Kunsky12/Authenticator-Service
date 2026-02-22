package com.mekheainteractive.authenticator_service.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

public class PlayFabService {

    @Value("${playfab.title-id}")
    private String titleId;

    @Value("${playfab.secret-key}")
    private String secretKey;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://" + titleId + ".playfabapi.com/Server")
                .defaultHeader("X-SecretKey", secretKey)
                .build();
    }
    // Verify PlayFab session ticket
    public String verifySessionTicket(String sessionTicket) {
        try {
            Map<String, Object> body = Map.of("SessionTicket", sessionTicket);

            Map response = restClient()
                    .post()
                    .uri("/AuthenticateSessionTicket")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                System.err.println("PlayFab response is null");
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> userInfo = (Map<String, Object>) data.get("UserInfo");

            if (userInfo == null) {
                System.err.println("PlayFab UserInfo is null");
                return null;
            }

            String playFabId = (String) userInfo.get("PlayFabId");
            System.out.println("Extracted PlayFabId " + playFabId);
            return playFabId;

        } catch (RestClientResponseException ex) {
            System.err.println("PlayFab verify session error: " + ex.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
