package com.fpoly.java6.services;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.fpoly.java6.entities.Category;
import com.fpoly.java6.entities.Image;
import com.fpoly.java6.entities.Product;
import com.fpoly.java6.repositories.CategoryRepository;
import com.fpoly.java6.repositories.ImageRepository;
import com.fpoly.java6.repositories.ProductRepository;
import com.fpoly.java6.requests.product.ProductUpsertRequest;
import com.fpoly.java6.responses.ApiResponse;
import com.fpoly.java6.responses.ProductResponse;

@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ImageRepository imageRepository;
	private final Path productImageDir;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
			ImageRepository imageRepository, @Value("${app.upload.product-image-dir:uploads/products}") String imageDir) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.imageRepository = imageRepository;
		this.productImageDir = Paths.get(imageDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.productImageDir);
		} catch (IOException ex) {
			throw new RuntimeException("Khong the tao thu muc luu anh san pham", ex);
		}
	}

	public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts() {
		ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
		List<ProductResponse> data = productRepository.findAll().stream().map(this::toProductResponse).toList();
		response.setMessage("Lay danh sach san pham thanh cong");
		response.setData(data);
		return ResponseEntity.ok(response);
	}

	public ResponseEntity<ApiResponse<ProductResponse>> getProductById(int id) {
		ApiResponse<ProductResponse> response = new ApiResponse<>();
		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isEmpty()) {
			response.setStatus(404);
			response.setMessage("Khong tim thay san pham");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		response.setMessage("Lay chi tiet san pham thanh cong");
		response.setData(toProductResponse(productOpt.get()));
		return ResponseEntity.ok(response);
	}

	@Transactional
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(ProductUpsertRequest request, Errors errors) {
		ApiResponse<ProductResponse> response = new ApiResponse<>();
		ResponseEntity<ApiResponse<ProductResponse>> invalidResponse = validateRequest(request, errors, response);
		if (invalidResponse != null) {
			return invalidResponse;
		}

		Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
		if (category == null) {
			response.setStatus(404);
			response.setMessage("Khong tim thay danh muc");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Product product = new Product();
		applyRequestToEntity(product, request, category);
		productRepository.save(product);
		ResponseEntity<ApiResponse<ProductResponse>> imageResponse = replaceProductImages(product, request.getImagesBase64(),
				response);
		if (imageResponse != null) {
			return imageResponse;
		}

		response.setStatus(201);
		response.setMessage("Them san pham thanh cong");
		response.setData(toProductResponse(product));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Transactional
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(int id, ProductUpsertRequest request,
			Errors errors) {
		ApiResponse<ProductResponse> response = new ApiResponse<>();
		ResponseEntity<ApiResponse<ProductResponse>> invalidResponse = validateRequest(request, errors, response);
		if (invalidResponse != null) {
			return invalidResponse;
		}

		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isEmpty()) {
			response.setStatus(404);
			response.setMessage("Khong tim thay san pham");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
		if (category == null) {
			response.setStatus(404);
			response.setMessage("Khong tim thay danh muc");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		Product product = productOpt.get();
		applyRequestToEntity(product, request, category);
		productRepository.save(product);
		ResponseEntity<ApiResponse<ProductResponse>> imageResponse = replaceProductImages(product, request.getImagesBase64(),
				response);
		if (imageResponse != null) {
			return imageResponse;
		}

		response.setMessage("Cap nhat san pham thanh cong");
		response.setData(toProductResponse(product));
		return ResponseEntity.ok(response);
	}

	@Transactional
	public ResponseEntity<ApiResponse<String>> deleteProduct(int id) {
		ApiResponse<String> response = new ApiResponse<>();
		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isEmpty()) {
			response.setStatus(404);
			response.setMessage("Khong tim thay san pham");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
		deleteImageFilesByProductId(id);
		imageRepository.deleteByProductId(id);
		productRepository.delete(productOpt.get());
		response.setMessage("Xoa san pham thanh cong");
		return ResponseEntity.ok(response);
	}

	private ResponseEntity<ApiResponse<ProductResponse>> validateRequest(ProductUpsertRequest request, Errors errors,
			ApiResponse<ProductResponse> response) {
		if (errors.hasErrors()) {
			response.setStatus(400);
			response.setMessage(errors.getAllErrors().get(0).getDefaultMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		for (String imageBase64 : request.getImagesBase64()) {
			if (!isValidBase64(imageBase64)) {
				response.setStatus(400);
				response.setMessage("Image base64 khong hop le");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
		}
		return null;
	}

	private void applyRequestToEntity(Product product, ProductUpsertRequest request, Category category) {
		product.setName(request.getName().trim());
		product.setPrice(request.getPrice());
		product.setQuantity(request.getQuantity());
		product.setStatus(request.getStatus());
		product.setCategory(category);
	}

	private boolean isValidBase64(String input) {
		if (input == null || input.isBlank()) {
			return false;
		}
		String base64Body = input.trim();
		if (base64Body.startsWith("data:")) {
			int index = base64Body.indexOf(",");
			if (index < 0 || index == base64Body.length() - 1) {
				return false;
			}
			base64Body = base64Body.substring(index + 1);
		}

		try {
			Base64.getDecoder().decode(base64Body);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private ResponseEntity<ApiResponse<ProductResponse>> replaceProductImages(Product product, List<String> imagesBase64,
			ApiResponse<ProductResponse> response) {
		deleteImageFilesByProductId(product.getId());
		imageRepository.deleteByProductId(product.getId());

		for (String imageBase64 : imagesBase64) {
			String fileName;
			try {
				fileName = saveBase64AsFile(imageBase64);
			} catch (IOException ex) {
				response.setStatus(500);
				response.setMessage("Luu file anh that bai");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

			Image image = new Image();
			image.setName(fileName);
			image.setProduct(product);
			imageRepository.save(image);
		}
		return null;
	}

	private void deleteImageFilesByProductId(int productId) {
		List<Image> oldImages = imageRepository.findByProductId(productId);
		for (Image oldImage : oldImages) {
			Path filePath = productImageDir.resolve(oldImage.getName());
			try {
				Files.deleteIfExists(filePath);
			} catch (IOException ex) {
				// Ignore missing/cannot-delete files to keep delete/update API resilient.
			}
		}
	}

	private String saveBase64AsFile(String imageBase64) throws IOException {
		String content = imageBase64.trim();
		String extension = detectExtension(content);
		String base64Body = content;
		if (content.startsWith("data:")) {
			base64Body = content.substring(content.indexOf(",") + 1);
		}
		byte[] bytes = Base64.getDecoder().decode(base64Body);
		String fileName = UUID.randomUUID() + "." + extension;
		Path filePath = productImageDir.resolve(fileName);
		Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);
		return fileName;
	}

	private String detectExtension(String content) {
		if (!content.startsWith("data:")) {
			return "png";
		}
		int semicolon = content.indexOf(";");
		if (semicolon < 0) {
			return "png";
		}
		String mime = content.substring(5, semicolon).toLowerCase();
		return switch (mime) {
		case "image/jpeg", "image/jpg" -> "jpg";
		case "image/gif" -> "gif";
		case "image/webp" -> "webp";
		case "image/bmp" -> "bmp";
		case "image/svg+xml" -> "svg";
		default -> "png";
		};
	}

	private ProductResponse toProductResponse(Product product) {
		List<String> imageNames = imageRepository.findByProductId(product.getId()).stream().map(Image::getName)
				.collect(Collectors.toList());
		return ProductResponse.from(product, imageNames);
	}
}
