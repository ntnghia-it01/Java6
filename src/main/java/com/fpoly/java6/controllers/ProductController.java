package com.fpoly.java6.controllers;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fpoly.java6.requests.product.ProductUpsertRequest;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.ProductResponse;
import com.fpoly.java6.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts() {
		return productService.getProducts();
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable int id) {
		return productService.getProductById(id);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> createProductJson(
			@Valid @RequestBody ProductUpsertRequest request, Errors errors) {
		return productService.createProduct(request, errors);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> createProductForm(
			@Valid @ModelAttribute ProductUpsertRequest request, Errors errors) {
		return productService.createProduct(request, errors);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProductJson(@PathVariable int id,
			@Valid @RequestBody ProductUpsertRequest request, Errors errors) {
		return productService.updateProduct(id, request, errors);
	}

	@PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProductForm(@PathVariable int id,
			@Valid @ModelAttribute ProductUpsertRequest request, Errors errors) {
		return productService.updateProduct(id, request, errors);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable int id) {
		return productService.deleteProduct(id);
	}
}
