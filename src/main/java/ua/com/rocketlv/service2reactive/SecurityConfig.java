package ua.com.rocketlv.service2reactive;

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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;
import reactor.netty.http.Http11SslContextSpec;
import ua.com.rocketlv.service2reactive.custom.CustomServerAuthorizationRequestResolver;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Objects;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/**").permitAll()
                        .pathMatchers("/users/**").authenticated()
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtConverter()))
                        .authenticationEntryPoint((exchange, ex) -> {
                            System.out.println("Authentication error: " + ex.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler((exchange, denied) -> {
                            System.out.println("Access denied: " + denied.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                ).oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(
                                new CustomServerAuthorizationRequestResolver(clientRegistrationRepository)
                        ));


        return http.build();
    }
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter() {
        return new Converter<Jwt, Mono<AbstractAuthenticationToken>>() {
            @Override
            public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
                System.out.println("JWT Converter invoked!");
                String email = jwt.getClaimAsString("email");

                // Validate email against allowed emails or domains
                if (!isEmailAllowed(email)) {
                    System.out.println("Access denied for email: " + email);
                    return Mono.error(new AccessDeniedException("Email not allowed: " + email));
                }

                return Mono.just(new JwtAuthenticationToken(jwt));
            }
        };
    }

    private boolean isEmailAllowed(String email) {
        if (email == null) return false;
        return Objects.equals("rocketlv1@gmail.com", email);
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