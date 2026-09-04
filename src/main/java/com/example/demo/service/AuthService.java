package com.example.demo.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;



import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.model.AppUser;
import com.example.demo.repository.UserRepository;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
    UserRepository userRepository,
        PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
                JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public AppUser register(
    String username,
        String password
  ) {
    if (userRepository.existsByUsername(username)) {
      throw new UsernameAlreadyExistsException(username);
    }

    String passwordHash = passwordEncoder.encode(password);

    AppUser user = new AppUser(
        username,
        passwordHash,
        "ROLE_USER");

    return userRepository.save(user);
  }
  
  public String login(String username, String password) {

    Authentication authentication =
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(username, password)
            );

    return jwtService.generateToken(authentication);
}
}
