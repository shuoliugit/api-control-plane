package com.example.apicontrolplane.credential;

import com.example.apicontrolplane.exception.ApiException;
import com.example.apicontrolplane.portal.DeveloperApplication;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredentialService {
  private final ApiCredentialRepository credentials;
  private final PasswordEncoder encoder;
  private final SecureRandom random = new SecureRandom();

  public CredentialService(ApiCredentialRepository credentials, PasswordEncoder encoder) {
    this.credentials = credentials;
    this.encoder = encoder;
  }

  public CredentialResponse generate(DeveloperApplication app) {
    if (credentials.findByApplicationId(app.getId()).isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "Credentials already exist for this application");
    }
    String clientId = "cp_" + UUID.randomUUID().toString().replace("-", "");
    String secret = secret();
    ApiCredential credential = credentials.save(new ApiCredential(app, clientId, encoder.encode(secret)));
    return CredentialResponse.created(credential, secret);
  }

  public CredentialResponse rotate(ApiCredential credential) {
    String secret = secret();
    credential.rotate(encoder.encode(secret));
    return CredentialResponse.created(credentials.save(credential), secret);
  }

  public boolean matches(ApiCredential credential, String secret) {
    return encoder.matches(secret, credential.getClientSecretHash());
  }

  private String secret() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return "cps_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public record CredentialResponse(String clientId, String clientSecret, String lastRotatedAt) {
    static CredentialResponse created(ApiCredential credential, String rawSecret) {
      return new CredentialResponse(credential.getClientId(), rawSecret, credential.getLastRotatedAt().toString());
    }
  }
}
