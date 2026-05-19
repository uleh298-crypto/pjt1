package com.ssafy.ssabre.campus.repository;

import com.ssafy.ssabre.campus.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClassesRepository extends JpaRepository<Classes, Long> {
    @Query("SELECT c FROM Classes c JOIN FETCH c.campus WHERE c.campus.id = :campusId")
    List<Classes> findByCampusId(Long campusId);

    java.util.Optional<Classes> findByCampusIdAndGenerationAndClassNoAndDeletedAtIsNull(
            Long campusId, Integer generation, Integer classNo);

    @Query("SELECT c FROM Classes c JOIN FETCH c.campus")
    List<Classes> findAllWithCampus();

    @Query("SELECT c FROM Classes c JOIN FETCH c.campus WHERE c.id = :id")
    Optional<Classes> findByIdWithCampus(Long id);
}
