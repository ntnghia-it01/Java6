package com.fpoly.java6.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.Date;

import com.fpoly.java6.entities.RevokedToken;
import com.fpoly.java6.entities.User;
import com.fpoly.java6.repositories.RevokedTokenRepository;
import com.fpoly.java6.repositories.UserRepository;
import com.fpoly.java6.requests.auth.LoginRequest;
import com.fpoly.java6.requests.auth.RefreshRequest;
import com.fpoly.java6.requests.auth.RegisterRequest;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.AuthResponse;
import com.fpoly.java6.responses.UserResponse;
import com.fpoly.java6.security.CustomUserDetails;
import com.fpoly.java6.security.CustomUserDetailsService;
import com.fpoly.java6.security.JwtService;
import com.fpoly.java6.security.TokenType;

@Service
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RevokedTokenRepository revokedTokenRepository;
	private final CustomUserDetailsService userDetailsService;

	public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService, RevokedTokenRepository revokedTokenRepository,
			CustomUserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.revokedTokenRepository = revokedTokenRepository;
		this.userDetailsService = userDetailsService;
	}

	public ResponseEntity<ApiResponse<UserResponse>> register(RegisterRequest request, Errors errors) {
		ApiResponse<UserResponse> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			response.setStatus(400);
			response.setMessage(errors.getAllErrors().get(0).getDefaultMessage());
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

	public ResponseEntity<ApiResponse<AuthResponse>> login(LoginRequest request, Errors errors) {
		ApiResponse<AuthResponse> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			response.setStatus(400);
			response.setMessage(errors.getAllErrors().get(0).getDefaultMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
			CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
			String accessToken = jwtService.generateAccessToken(principal);
			String refreshToken = jwtService.generateRefreshToken(principal);

			response.setMessage("Dang nhap thanh cong");
			response.setData(new AuthResponse(accessToken, refreshToken, UserResponse.from(principal.getUser())));
			return ResponseEntity.ok(response);
		} catch (AuthenticationException ex) {
			response.setStatus(401);
			response.setMessage("Sai username hoac password");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	public ResponseEntity<ApiResponse<AuthResponse>> refresh(RefreshRequest request, Errors errors) {
		ApiResponse<AuthResponse> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			response.setStatus(400);
			response.setMessage(errors.getAllErrors().get(0).getDefaultMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		String refreshToken = request.getRefreshToken();
		String accessToken = request.getAccessToken();

		if (revokedTokenRepository.existsByToken(refreshToken) || revokedTokenRepository.existsByToken(accessToken)) {
			response.setStatus(401);
			response.setMessage("Token da bi revoke");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username;
		try {
			username = jwtService.extractUsername(refreshToken);
		} catch (Exception ex) {
			response.setStatus(401);
			response.setMessage("Refresh token khong hop le");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
		if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
			response.setStatus(401);
			response.setMessage("Refresh token khong hop le");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		try {
			String accessUsername = jwtService.extractUsernameAllowExpired(accessToken);
			if (!username.equals(accessUsername)) {
				response.setStatus(401);
				response.setMessage("Access token khong hop le");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
			if (jwtService.extractTokenTypeAllowExpired(accessToken) != TokenType.ACCESS) {
				response.setStatus(401);
				response.setMessage("Access token khong hop le");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
		} catch (Exception ex) {
			response.setStatus(401);
			response.setMessage("Access token khong hop le");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		revokeTokenIfNeeded(accessToken, TokenType.ACCESS);
		revokeTokenIfNeeded(refreshToken, TokenType.REFRESH);

		String newAccess = jwtService.generateAccessToken(userDetails);
		String newRefresh = jwtService.generateRefreshToken(userDetails);

		response.setMessage("Refresh token thanh cong");
		response.setData(new AuthResponse(newAccess, newRefresh, UserResponse.from(userDetails.getUser())));
		return ResponseEntity.ok(response);
	}

	private void revokeTokenIfNeeded(String token, TokenType tokenType) {
		if (revokedTokenRepository.existsByToken(token)) {
			return;
		}
		RevokedToken revoked = new RevokedToken();
		revoked.setToken(token);
		revoked.setTokenType(tokenType);
		revoked.setRevokedAt(new Date());
		revoked.setExpiresAt(jwtService.extractExpirationAllowExpired(token));
		revokedTokenRepository.save(revoked);
	}
}
