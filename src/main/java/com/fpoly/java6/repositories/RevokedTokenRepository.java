package com.fpoly.java6.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java6.entities.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Integer> {
	boolean existsByToken(String token);
}
