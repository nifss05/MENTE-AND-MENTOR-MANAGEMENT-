package org.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiry(System.currentTimeMillis() + 1000 * 60 * 10);

        userRepository.save(user);

        // Send reset email
        try {
            emailService.sendResetEmail(email, token);
            return ResponseEntity.ok("Reset link sent to your email");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to send reset email. Please try again later.");
        }
    }

    @GetMapping("/reset-password")
    public void showResetPage(@RequestParam String token,
            HttpServletResponse response) throws IOException {

        response.sendRedirect("/reset_password.html?token=" + token);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
            @RequestParam String newPassword) {

        User user = userRepository.findByResetToken(token);

        if (user == null) {
            return ResponseEntity.badRequest().body(
                    "Invalid token");
        }

        if (user.getTokenExpiry() < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(
                    "Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                "Password updated successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session) {

        // Login logic remains the same
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())
                || !user.getRole().equalsIgnoreCase(role)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        session.setAttribute("user", user);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String role) {

        if (username == null || username.length() != 10) {
            return ResponseEntity.badRequest().body("USN must be exactly 10 characters long.");
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("Password and Confirm Password must match.");
        }

        if (userRepository.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        if (userRepository.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        newUser.setEmail(email);
        newUser.setUsn(username);
        userRepository.save(newUser);

        return ResponseEntity.ok("Signup successful");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");

    }

}