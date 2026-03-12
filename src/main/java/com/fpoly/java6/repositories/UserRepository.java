package com.fpoly.java6.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java6.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
