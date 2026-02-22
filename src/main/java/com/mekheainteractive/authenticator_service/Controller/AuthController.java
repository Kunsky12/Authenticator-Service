package com.mekheainteractive.authenticator_service.Controller;

import com.mekheainteractive.authenticator_service.Service.JwtService;
import com.mekheainteractive.authenticator_service.Service.PlayFabService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final PlayFabService playFabService;
    private final JwtService jwtService;

    public AuthController(
            PlayFabService playFabService,
            JwtService jwtService
    ) {
        this.playFabService = playFabService;
        this.jwtService = jwtService;
    }
    // Authenticate SessionTicket from Client
    @PostMapping("/auth/login")
    public String login(@RequestBody LoginRequestDTO request) {
        String sessionTicket = playFabService.verifySessionTicket(request.getSessionTicket());
        return jwtService.generateToken(sessionTicket);
    }

    @Data
    @NoArgsConstructor
    public static class LoginRequestDTO {
        private String sessionTicket;
    }
}
