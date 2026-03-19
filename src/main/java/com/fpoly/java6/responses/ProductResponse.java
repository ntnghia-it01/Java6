package com.fpoly.java6.responses;

import java.util.List;

import com.fpoly.java6.entities.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductResponse {
	private int id;
	private String name;
	private int price;
	private int quantity;
	private boolean status;
	private int categoryId;
	private String categoryName;
	private List<String> images;

	public static ProductResponse from(Product product, List<String> images) {
		return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getQuantity(),
				product.isStatus(), product.getCategory().getId(), product.getCategory().getName(), images);
	}
}
