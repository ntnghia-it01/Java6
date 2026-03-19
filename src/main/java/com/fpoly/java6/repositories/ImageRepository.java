package com.fpoly.java6.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java6.entities.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {
	List<Image> findByProductId(int productId);

	void deleteByProductId(int productId);
}
