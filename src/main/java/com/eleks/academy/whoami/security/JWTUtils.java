package com.eleks.academy.whoami.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Configuration
@Component
public class JWTUtils {

	@Value("${jwt_secret}")
	private String secret;
	@Value("${jwt_issuer}")
	private String issuer;

	public String generateToken(String email) {
		Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());
		return JWT.create()
				.withSubject("Player details")
				.withClaim("email", email)
				.withIssuedAt(new Date())
				.withIssuer(issuer)
				.withExpiresAt(expirationDate)
				.sign(Algorithm.HMAC256(secret));
	}

	public String validateTokenAndRetrieveClaim(String token) {
		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
				.withSubject("Player details")
				.withIssuer(issuer)
				.build();
		DecodedJWT jwt = verifier.verify(token);
		return jwt.getClaim("email").asString();
	}
}
