package com.ssafy.ssabre.project.service;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.portfolio.entity.Portfolio;
import com.ssafy.ssabre.portfolio.repository.PortfolioRepository;
import com.ssafy.ssabre.project.dto.ProjectCreateRequest;
import com.ssafy.ssabre.project.dto.ProjectListResponse;
import com.ssafy.ssabre.project.dto.ProjectUpdateRequest;
import com.ssafy.ssabre.project.entity.Project;
import com.ssafy.ssabre.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public Project create(ProjectCreateRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        if (!portfolio.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You are not the owner of this portfolio");
        }

        Project project = Project.builder()
                .portfolio(portfolio)
                .title(request.title())
                .introduction(request.introduction())
                .description(request.description())
                .techStacks(request.techStacks() != null ? request.techStacks() : new ArrayList<>())
                .urls(request.urls() != null ? request.urls() : new ArrayList<>())
                .imageUrls(request.imageUrls() != null ? request.imageUrls() : new ArrayList<>())
                .build();

        return projectRepository.save(project);
    }

    public List<Project> findAll() {
        return projectRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id);
    }

    /**
     * 포트폴리오별 프로젝트 목록 조회 (DTO 변환을 트랜잭션 내에서 수행하여 LAZY 로딩 문제 방지)
     */
    @Transactional(readOnly = true)
    public ProjectListResponse findByPortfolioId(Long portfolioId) {
        List<Project> projects = projectRepository.findByPortfolio_IdAndDeletedAtIsNullOrderByCreatedAtDesc(portfolioId);
        // 트랜잭션 내에서 ElementCollection 초기화 후 DTO로 변환
        projects.forEach(project -> {
            project.getTechStacks().size();
            project.getUrls().size();
            project.getImageUrls().size();
        });
        return ProjectListResponse.from(projects);
    }

    @Transactional
    public void update(Long projectId, ProjectUpdateRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getPortfolio().getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You are not the owner of this project");
        }

        project.update(
                request.title(),
                request.introduction(),
                request.description(),
                request.techStacks(),
                request.urls(),
                request.imageUrls()
        );
    }

    @Transactional
    public void delete(Long projectId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getPortfolio().getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("You are not the owner of this project");
        }

        project.delete();
    }
}
