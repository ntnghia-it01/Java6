package com.fpoly.java6.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java6.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
