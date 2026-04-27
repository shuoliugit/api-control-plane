package com.example.apicontrolplane.auth;

import java.util.UUID;

public record CurrentUser(UUID id, String email, String role) {}
