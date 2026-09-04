package com.example.demo.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder jwtEncoder;
  private final long expirationSeconds;

  public JwtService(
    JwtEncoder jwtEncoder,
        @Value("${app.jwt.expiration-seconds}")
        long expirationSeconds
  ) {
    this.jwtEncoder = jwtEncoder;
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(
    Authentication authentication
  ) {
    Instant now = Instant.now();

    List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    JwtClaimsSet claims = JwtClaimsSet.builder().subject(authentication.getName()).issuedAt(now)
        .expiresAt(now.plusSeconds(expirationSeconds)).claim("authorities", authorities).build();

    return jwtEncoder.encode(
        JwtEncoderParameters.from(claims)).getTokenValue();
  }
  
  public long getExpirationSeconds() {
    return expirationSeconds;
  }
}
