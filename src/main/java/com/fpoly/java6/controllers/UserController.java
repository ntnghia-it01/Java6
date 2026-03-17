package com.fpoly.java6.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.fpoly.java6.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

//	API lấy danh sách user 
//	URL = GET /users 
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<UserResponse>>> userList() {
		return userService.getUsers();
	}

//	API lấy chi tiết user
//	URL = GET /users/1
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == principal.id")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable int id) {
		return userService.getUserById(id);
	}

//	API thêm user
//	URL = POST /users
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> addUserJson(@Valid @RequestBody UserPost user, Errors errors) {
		return userService.addUser(user, errors);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> addUserForm(@Valid @ModelAttribute UserPost user, Errors errors) {
		return userService.addUser(user, errors);
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
