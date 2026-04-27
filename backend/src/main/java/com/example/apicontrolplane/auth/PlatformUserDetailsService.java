package com.example.apicontrolplane.auth;

import com.example.apicontrolplane.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PlatformUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public PlatformUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return users.findByEmail(username.toLowerCase())
        .map(UserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
