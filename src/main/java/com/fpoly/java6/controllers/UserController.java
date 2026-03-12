package com.fpoly.java6.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpoly.java6.requests.user.UserPost;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.UserResponse;
import com.fpoly.java6.entities.User;
import com.fpoly.java6.repositories.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

//	API lấy danh sách user 
//	URL = GET /users 
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<UserResponse>>> userList() {
		ApiResponse<List<UserResponse>> response = new ApiResponse<>();
		List<UserResponse> data = userRepository.findAll().stream().map(UserResponse::from).toList();
		response.setData(data);
		response.setMessage("Lay danh sach user thanh cong");
		return ResponseEntity.ok(response);
	}

//	API lấy chi tiết user
//	URL = GET /users/1
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == principal.id")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable int id) {
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

//	API thêm user
//	URL = POST /users
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> addUserJson(@Valid @RequestBody UserPost user, Errors errors) {
		return addUserInternal(user, errors);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> addUserForm(@Valid @ModelAttribute UserPost user, Errors errors) {
		return addUserInternal(user, errors);
	}

	private ResponseEntity<ApiResponse<String>> addUserInternal(UserPost user, Errors errors) {
		ApiResponse<String> response = new ApiResponse<String>();
		if (errors.hasErrors()) {
//			trả error về json 
//			Chỉ trả về 1 lỗi duy nhất tại 1 thời điểm 
			String error = errors.getAllErrors().get(0).getDefaultMessage();
			response.setStatus(400);
			response.setMessage(error);

//			return ResponseEntity.ok(response);

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

		response.setMessage("Thêm user thành công");
//		return ResponseEntity.ok(response); // => http status == 200

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
//		=> http status == 201
	}

////	API sửa user
////	PATCH
//	@PutMapping("/{id}")
//	public ResponseEntity<T> updateUser() {
//
//	}
//
////	API xoá user
//	@DeleteMapping("/{id}")
//	public ResponseEntity<T> deleteUser() {
//
//	}

}
