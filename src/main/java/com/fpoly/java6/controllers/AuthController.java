package com.fpoly.java6.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpoly.java6.requests.auth.LoginRequest;
import com.fpoly.java6.requests.auth.RefreshRequest;
import com.fpoly.java6.requests.auth.RegisterRequest;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.AuthResponse;
import com.fpoly.java6.responses.UserResponse;
import com.fpoly.java6.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<UserResponse>> registerJson(@Valid @RequestBody RegisterRequest request,
			Errors errors) {
		return authService.register(request, errors);
	}

	@PostMapping(value = "/register", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ApiResponse<UserResponse>> registerForm(@Valid @ModelAttribute RegisterRequest request,
			Errors errors) {
		return authService.register(request, errors);
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<AuthResponse>> loginJson(@Valid @RequestBody LoginRequest request,
			Errors errors) {
		return authService.login(request, errors);
	}

	@PostMapping(value = "/login", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ApiResponse<AuthResponse>> loginForm(@Valid @ModelAttribute LoginRequest request,
			Errors errors) {
		return authService.login(request, errors);
	}

	@PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<AuthResponse>> refreshJson(@Valid @RequestBody RefreshRequest request,
			Errors errors) {
		return authService.refresh(request, errors);
	}

	@PostMapping(value = "/refresh", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ApiResponse<AuthResponse>> refreshForm(@Valid @ModelAttribute RefreshRequest request,
			Errors errors) {
		return authService.refresh(request, errors);
	}
}
