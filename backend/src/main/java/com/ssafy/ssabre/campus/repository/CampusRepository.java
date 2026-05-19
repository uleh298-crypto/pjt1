package com.ssafy.ssabre.campus.repository;

import com.ssafy.ssabre.campus.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusRepository extends JpaRepository<Campus, Long> {
}
