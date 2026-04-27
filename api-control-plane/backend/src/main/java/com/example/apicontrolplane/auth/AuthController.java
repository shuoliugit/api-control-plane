package com.example.apicontrolplane.auth;

import com.example.apicontrolplane.exception.ApiException;
import com.example.apicontrolplane.user.Role;
import com.example.apicontrolplane.user.UserAccount;
import com.example.apicontrolplane.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtService jwt;

  public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwt = jwt;
  }

  @PostMapping("/register")
  AuthResponse register(@RequestBody @Valid RegisterRequest request) {
    if (users.existsByEmail(request.email().toLowerCase())) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
    }
    UserAccount user = users.save(new UserAccount(request.email(), encoder.encode(request.password()), request.displayName(), Role.DEVELOPER));
    return AuthResponse.from(user, jwt.issue(user));
  }

  @PostMapping("/login")
  AuthResponse login(@RequestBody @Valid LoginRequest request) {
    authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
    UserAccount user = users.findByEmail(request.email().toLowerCase()).orElseThrow();
    return AuthResponse.from(user, jwt.issue(user));
  }

  @GetMapping("/me")
  CurrentUser me(@AuthenticationPrincipal CurrentUser user) {
    return user;
  }

  public record RegisterRequest(@Email String email, @Size(min = 8, max = 120) String password, @NotBlank @Size(max = 120) String displayName) {}
  public record LoginRequest(@Email String email, @NotBlank String password) {}
  public record AuthResponse(String token, String userId, String email, String displayName, Role role) {
    static AuthResponse from(UserAccount user, String token) {
      return new AuthResponse(token, user.getId().toString(), user.getEmail(), user.getDisplayName(), user.getRole());
    }
  }
}
