package com.fpoly.java6.requests.product;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductUpsertRequest {
	@NotBlank
	private String name;

	@NotNull
	@Min(0)
	private Integer price;

	@NotNull
	@Min(0)
	private Integer quantity;

	@NotNull
	private Boolean status;

	@NotNull
	private Integer categoryId;

	@NotEmpty
	private List<@NotBlank String> imagesBase64;
}
