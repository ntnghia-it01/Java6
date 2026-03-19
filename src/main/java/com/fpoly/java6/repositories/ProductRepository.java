package com.fpoly.java6.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java6.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
