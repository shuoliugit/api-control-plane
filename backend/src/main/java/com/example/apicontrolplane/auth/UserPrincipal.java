package com.example.apicontrolplane.auth;

import com.example.apicontrolplane.user.UserAccount;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(UserAccount user) implements UserDetails {
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getPassword() { return user.getPasswordHash(); }
  @Override
  public String getUsername() { return user.getEmail(); }
}
