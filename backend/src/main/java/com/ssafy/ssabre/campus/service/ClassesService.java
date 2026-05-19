package com.ssafy.ssabre.campus.service;

import com.ssafy.ssabre.campus.dto.ClassesRequestDto;
import com.ssafy.ssabre.campus.dto.ClassesResponseDto;
import com.ssafy.ssabre.campus.entity.Classes;
import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.repository.ClassesRepository;
import com.ssafy.ssabre.campus.entity.Enrollment;
import com.ssafy.ssabre.campus.repository.CampusRepository;
import com.ssafy.ssabre.campus.repository.EnrollmentRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassesService {

        private final ClassesRepository classesRepository;
        private final CampusRepository campusRepository;
        private final MemberRepository memberRepository;
        private final EnrollmentRepository enrollmentRepository;

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = { "classes", "campusClasses" }, allEntries = true)
        public Long save(ClassesRequestDto requestDto) {
                Campus campus = campusRepository.findById(requestDto.getCampusId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "해당 캠퍼스가 존재하지 않습니다. id=" + requestDto.getCampusId()));

                Classes classes = Classes.builder()
                                .campus(campus)
                                .name(requestDto.getName())
                                .generation(requestDto.getGeneration())
                                .classNo(requestDto.getClassNo())
                                .trackType(requestDto.getTrackType())
                                .build();

                return classesRepository.save(classes).getId();
        }

        public ClassesResponseDto findById(Long id) {
                Classes classes = classesRepository.findByIdWithCampus(id)
                                .orElseThrow(() -> new IllegalArgumentException("해당 반이 존재하지 않습니다. id=" + id));
                return new ClassesResponseDto(classes);
        }

        @org.springframework.cache.annotation.Cacheable(value = "classes")
        public List<ClassesResponseDto> findAll() {
                return classesRepository.findAllWithCampus().stream()
                                .map(ClassesResponseDto::new)
                                .collect(Collectors.toList());
        }

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = { "classes", "campusClasses" }, allEntries = true)
        public void update(Long id, ClassesRequestDto requestDto) {
                Classes classes = classesRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("해당 반이 존재하지 않습니다. id=" + id));

                Campus campus = campusRepository.findById(requestDto.getCampusId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "해당 캠퍼스가 존재하지 않습니다. id=" + requestDto.getCampusId()));

                classes.update(campus, requestDto.getName(), requestDto.getGeneration(), requestDto.getClassNo(),
                                requestDto.getTrackType());
        }

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = { "classes", "campusClasses" }, allEntries = true)
        public void delete(Long id) {
                Classes classes = classesRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("해당 반이 존재하지 않습니다. id=" + id));
                classes.delete();
        }

        @Transactional
        public void enroll(Long classesId, Long memberId) {
                Classes classes = classesRepository.findById(classesId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 반이 존재하지 않습니다. id=" + classesId));

                Member member = memberRepository.findById(memberId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. id=" + memberId));

                Enrollment enrollment = Enrollment.builder()
                                .classes(classes)
                                .member(member)
                                .build();
                enrollmentRepository.save(enrollment);
        }
}
