package org.example.microservices.UserServer.controller;

import org.example.microservices.UserServer.dao.security.RoleRepository;
import org.example.microservices.UserServer.dao.security.UserRepository;
import org.example.microservices.UserServer.entity.security.Role;
import org.example.microservices.UserServer.entity.security.User;
import org.example.microservices.UserServer.payload.security.JWTAuthResponse;
import org.example.microservices.UserServer.payload.security.LoginDto;
import org.example.microservices.UserServer.payload.security.SignUpDto;
import org.example.microservices.UserServer.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * @author b1go
 * @date 6/26/22 5:03 PM
 * Updated with correct package names
 */
@RestController
@RequestMapping("/api/v1/auth/jwt")
public class AuthJWTController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(AuthJWTController.class);

    @CrossOrigin(origins = "*")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginDto) {
        logger.info(loginDto.getAccountOrEmail() + " is trying to sign in our application");

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDto.getAccountOrEmail(), loginDto.getPassword()
            ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // get token from tokenProvider
            String token = tokenProvider.generateToken(authentication);
            JWTAuthResponse jwtAuthResponse = new JWTAuthResponse(token);
            jwtAuthResponse.setTokenType("JWT");

            logger.info(loginDto.getAccountOrEmail() + " signed in successfully");
            return ResponseEntity.ok(jwtAuthResponse);
            
        } catch (BadCredentialsException ex) {
            logger.warn("Authentication failed for user: " + loginDto.getAccountOrEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid username or password"));
        } catch (AuthenticationException ex) {
            logger.error("Authentication error: " + ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Authentication failed: " + ex.getMessage()));
        }
    }

    @CrossOrigin(origins = "*") // Allow requests from this origin
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {
        logger.info("New User is trying to sign up our application");

        // check if username is in a DB
        if (userRepository.existsByAccount(signUpDto.getAccount())) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // check if email exists in DB
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setName(signUpDto.getName());
        user.setAccount(signUpDto.getAccount());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        Role roles = null;
        if (signUpDto.getAccount().contains("chuwa")) {
            roles = roleRepository.findByName("ROLE_ADMIN").get();
        } else {
            roles = roleRepository.findByName("ROLE_USER").get();
        }

        user.setRoles(Collections.singleton(roles));
        userRepository.save(user);

        logger.info("User registered successfully");
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
}
