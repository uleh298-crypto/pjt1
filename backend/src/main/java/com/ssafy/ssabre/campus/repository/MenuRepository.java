package com.ssafy.ssabre.campus.repository;

import com.ssafy.ssabre.campus.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByCampusId(Long campusId);
    List<Menu> findByDateAndDeletedAtIsNull(LocalDate date);
    List<Menu> findByCampusIdAndDateAndDeletedAtIsNull(Long campusId, LocalDate date);
}
