package com.ssafy.ssabre.campus.service;

import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.entity.Menu;
import com.ssafy.ssabre.campus.repository.CampusRepository;
import com.ssafy.ssabre.campus.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampusService {

    private final CampusRepository campusRepository;
    private final com.ssafy.ssabre.campus.repository.ClassesRepository classesRepository;
    private final MenuRepository menuRepository;

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = { "campuses", "campusClasses" }, allEntries = true)
    public Long save(String name) {
        Campus saved = campusRepository.save(new Campus(name));
        return saved.getId();
    }

    @org.springframework.cache.annotation.Cacheable(value = "campuses")
    public List<Campus> findAll() {
        return campusRepository.findAll();
    }

    public java.util.List<com.ssafy.ssabre.campus.entity.Classes> findClassesByCampusId(Long campusId) {
        return classesRepository.findByCampusId(campusId);
    }

    @Transactional
    public Menu createMenu(Long campusId, LocalDate date, String imageUrl) {
        Campus campus = campusRepository.findById(campusId)
                .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + campusId));

        Menu menu = Menu.builder()
                .campus(campus)
                .date(date)
                .imageUrl(imageUrl)
                .build();

        return menuRepository.save(menu);
    }
}
