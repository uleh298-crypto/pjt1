package com.ssafy.ssabre.portfolio.repository;

import com.ssafy.ssabre.portfolio.entity.Stack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StackRepository extends JpaRepository<Stack, Long> {
}
