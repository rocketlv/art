package ua.com.rocketlv.service2reactive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ua.com.rocketlv.service2reactive.dto.AdmResponseDto;
import ua.com.rocketlv.service2reactive.exceptions.UserNotFoundException;
import ua.com.rocketlv.service2reactive.dto.UserDto;
import ua.com.rocketlv.service2reactive.services.RemoteApiService;
import ua.com.rocketlv.service2reactive.services.UserService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")

public class UserController {
    private final UserService userService;
    private final RemoteApiService remoteApiService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<UserDto> getAllUsers() {
        return userService.getAllUsers().delayElements(Duration.ofSeconds(0));
    }

    @CrossOrigin("*")
    @GetMapping("/{id}")
    public Mono<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserWithLogs(id);
    }

    @GetMapping("/city/{city}")
    public Flux<UserDto> getUsersByCity(@PathVariable String city) {
        return userService.getUsersByCity(city);
    }

    @GetMapping("/remote")
    public Flux<AdmResponseDto> getRemoteUsers() {
        return remoteApiService.getRemoteApiService();
    }


    final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/id-token")
    public Mono<Map<String, String>> getIdToken(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return Mono.error(new UserNotFoundException("No OidcUser available"));
        }
        return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
                .cast(OAuth2AuthenticationToken.class).flatMap(authentication -> {
                    String idTokenValue = oidcUser.getIdToken().getTokenValue();
                    Map<String, Object> claims = oidcUser.getUserInfo().getClaims();
                    String email = (String) claims.get("email");
                    String fullName = (String) claims.get("name");

                    Map<String, String> tokenMap = new HashMap<>();
                    tokenMap.put("id_token", idTokenValue);
                    tokenMap.put("email", email);
                    tokenMap.put("full_name", fullName);
                    return Mono.just(tokenMap).flatMap(tokenMp -> {
                        return authorizedClientService.loadAuthorizedClient(
                                "google", authentication.getName()).switchIfEmpty(
                                Mono.error(new UserNotFoundException(authentication.getName()))
                        ).flatMap(authorizedClient -> {
                            String refreshToken = null;
                            if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
                                OAuth2RefreshToken refreshTokenOAuth = authorizedClient.getRefreshToken();
                                refreshToken = refreshTokenOAuth.getTokenValue();
                            }
                            tokenMp.put("refresh_token", refreshToken);
                            return Mono.just(tokenMp);
                        });
                    });
                });
    }
}
