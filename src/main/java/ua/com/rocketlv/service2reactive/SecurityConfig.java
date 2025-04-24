package ua.com.rocketlv.service2reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;
import reactor.netty.http.Http11SslContextSpec;
import ua.com.rocketlv.service2reactive.custom.CustomServerAuthorizationRequestResolver;
import ua.com.rocketlv.service2reactive.exceptions.ErrorResponse;
import ua.com.rocketlv.service2reactive.repo.UserRepository;
import ua.com.rocketlv.service2reactive.services.UserService;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    final UserService userService;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {
        ObjectMapper objectMapper = new ObjectMapper();
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/**").permitAll()
                        .pathMatchers("/users/**").access(emailAuthorizationManager())
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((exchange, ex) -> {
                            System.out.println("Access denied: " + ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            try {
                                return exchange.getResponse().writeWith(
                                        Mono.just(exchange.getResponse().bufferFactory()
                                                .wrap(objectMapper.writeValueAsBytes(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()))))
                                );
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(
                                new CustomServerAuthorizationRequestResolver(clientRegistrationRepository)
                        ));
        return http.build();
    }

    @Bean
    public ReactiveAuthorizationManager<AuthorizationContext> emailAuthorizationManager() {
        Set<String> allowedEmailsSet = userService.getAllUsers().map(val->val.getName()).collect(Collectors.toSet()).block();
        
        return (authenticationMono, context) -> authenticationMono
                .filter(Authentication::isAuthenticated)
                .doOnNext(auth -> log.info("Authentication type: {}", auth.getName()))
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .map(token -> {
                    log.info("------ OAuth2 Authentication Token: {}", token);
                    OAuth2User oauth2User = token.getPrincipal();
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    log.info("OAuth2 Attributes: {}", attributes);
                    String email = (String) attributes.get("email");
                    log.info("Oauth2 token email: {}", email);

                    boolean granted = email != null &&
                            allowedEmailsSet.contains(email.toLowerCase());

                    log.info("Access attempt: email='{}', granted={}", email, granted);

                    return new AuthorizationDecision(granted);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Access denied: No OAuth2AuthenticationToken found");
                    return Mono.just(new AuthorizationDecision(false));
                }))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    @Bean
    public CustomServerAuthorizationRequestResolver customServerAuthorizationRequestResolver(ReactiveClientRegistrationRepository repo) {
        // Make sure this class exists and is configured correctly if needed
        return new CustomServerAuthorizationRequestResolver(repo);
    }


    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientService authorizedClientService(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        factory.addServerCustomizers(httpServer -> {
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(new FileInputStream("src/main/resources/keystore.p12"), "password".toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, "password".toCharArray());

//                SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyManagerFactory);
                Http11SslContextSpec sslContextSpec = Http11SslContextSpec.forServer(keyManagerFactory);

                return httpServer.secure(ssl -> ssl.sslContext(sslContextSpec));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return factory;
    }

//    @Bean
//    public NettyReactiveWebServerFactory httpRedirectServerFactory() {
//        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(8080);
//        factory.addServerCustomizers(httpServer -> httpServer.route(routes ->
//                routes.route(req -> true, (req, res) -> {
//                    String host = req.requestHeaders().get("Host");
//                    if (host != null && host.contains(":")) {
//                        host = host.split(":")[0];
//                    }
//                    String redirectUri = "https://" + host + ":8443" + req.uri();
//                    res.status(HttpResponseStatus.MOVED_PERMANENTLY);
//                    res.header(HttpHeaderNames.LOCATION, redirectUri);
//                    return res.send();
//                })
//        ));
//        return factory;
//    }
}