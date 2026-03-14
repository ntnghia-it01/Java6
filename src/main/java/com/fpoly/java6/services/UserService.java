package com.fpoly.java6.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import com.fpoly.java6.entities.User;
import com.fpoly.java6.repositories.UserRepository;
import com.fpoly.java6.requests.user.UserPost;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.UserResponse;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
		ApiResponse<List<UserResponse>> response = new ApiResponse<>();
		List<UserResponse> data = userRepository.findAll().stream().map(UserResponse::from).toList();
		response.setData(data);
		response.setMessage("Lay danh sach user thanh cong");
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<ApiResponse<UserResponse>> getUserById(int id) {
		ApiResponse<UserResponse> response = new ApiResponse<>();
		Optional<User> userOpt = userRepository.findById(id);
		if (userOpt.isEmpty()) {
			response.setStatus(404);
			response.setMessage("Khong tim thay user");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		response.setData(UserResponse.from(userOpt.get()));
		response.setMessage("Lay thong tin user thanh cong");
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<ApiResponse<String>> addUser(UserPost user, Errors errors) {
		ApiResponse<String> response = new ApiResponse<>();
		if (errors.hasErrors()) {
			response.setStatus(400);
			response.setMessage(errors.getAllErrors().get(0).getDefaultMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		if (userRepository.existsByUsername(user.getUsername())) {
			response.setStatus(409);
			response.setMessage("Username da ton tai");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}
		if (userRepository.existsByEmail(user.getEmail())) {
			response.setStatus(409);
			response.setMessage("Email da ton tai");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
		}

		User entity = new User();
		entity.setUsername(user.getUsername().trim());
		entity.setPassword(passwordEncoder.encode(user.getPassword()));
		entity.setName(user.getName().trim());
		entity.setEmail(user.getEmail().trim());
		entity.setStatus(true);
		entity.setRole(0);
		userRepository.save(entity);

		response.setMessage("Them user thanh cong");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
