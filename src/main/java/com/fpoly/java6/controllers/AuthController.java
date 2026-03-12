package com.fpoly.java6.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpoly.java6.entities.User;
import com.fpoly.java6.repositories.UserRepository;
import com.fpoly.java6.requests.auth.LoginRequest;
import com.fpoly.java6.requests.auth.RegisterRequest;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.AuthResponse;
import com.fpoly.java6.responses.UserResponse;
import com.fpoly.java6.security.CustomUserDetails;
import com.fpoly.java6.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<UserResponse>> registerJson(@Valid @RequestBody RegisterRequest request,
			Errors errors) {
		return registerInternal(request, errors);
	}

	@PostMapping(value = "/register", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ApiResponse<UserResponse>> registerForm(@Valid @ModelAttribute RegisterRequest request,
			Errors errors) {
		return registerInternal(request, errors);
	}

	private ResponseEntity<ApiResponse<UserResponse>> registerInternal(RegisterRequest request, Errors errors) {
		ApiResponse<UserResponse> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			String error = errors.getAllErrors().get(0).getDefaultMessage();
			response.setStatus(400);
			response.setMessage(error);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (userRepository.existsByUsername(request.getUsername())) {
			response.setStatus(409);
			response.setMessage("Username da ton tai");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			response.setStatus(409);
			response.setMessage("Email da ton tai");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		User user = new User();
		user.setUsername(request.getUsername().trim());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getName().trim());
		user.setEmail(request.getEmail().trim());
		user.setStatus(true);
		user.setRole(0);
		userRepository.save(user);

		response.setStatus(201);
		response.setMessage("Dang ky thanh cong");
		response.setData(UserResponse.from(user));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<AuthResponse>> loginJson(@Valid @RequestBody LoginRequest request,
			Errors errors) {
		return loginInternal(request, errors);
	}

	@PostMapping(value = "/login", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ApiResponse<AuthResponse>> loginForm(@Valid @ModelAttribute LoginRequest request,
			Errors errors) {
		return loginInternal(request, errors);
	}

	private ResponseEntity<ApiResponse<AuthResponse>> loginInternal(LoginRequest request, Errors errors) {
		ApiResponse<AuthResponse> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			String error = errors.getAllErrors().get(0).getDefaultMessage();
			response.setStatus(400);
			response.setMessage(error);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
			CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
			String token = jwtService.generateToken(principal);

			response.setMessage("Dang nhap thanh cong");
			response.setData(new AuthResponse(token, UserResponse.from(principal.getUser())));
			return ResponseEntity.ok(response);
		} catch (AuthenticationException ex) {
			response.setStatus(401);
			response.setMessage("Sai username hoac password");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}
}
