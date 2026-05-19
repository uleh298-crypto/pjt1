package com.ssafy.ssabre.portfolio.service;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.portfolio.dto.PortfolioCreateRequest;
import com.ssafy.ssabre.portfolio.dto.PortfolioResponse;
import com.ssafy.ssabre.portfolio.dto.PortfolioResponse.SolvedAcInfo;
import com.ssafy.ssabre.portfolio.dto.PortfolioUpdateRequest;
import com.ssafy.ssabre.portfolio.dto.SolvedAcUserResponse;
import com.ssafy.ssabre.portfolio.entity.*;
import com.ssafy.ssabre.portfolio.repository.*;
import com.ssafy.ssabre.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StackRepository stackRepository;
    private final PortfolioStackRepository portfolioStackRepository;
    private final PortfolioUrlRepository portfolioUrlRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final MemberRepository memberRepository;
    private final UploadService uploadService;
    private final SolvedAcService solvedAcService;

    @Transactional
    public Long createPortfolio(PortfolioCreateRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Portfolio portfolio = Portfolio.builder()
                .member(member)
                .title(request.title())
                .description(request.description())
                .introduction(request.introduction())
                .bojHandle(request.bojHandle())
                .swTestRank(request.swTestRank())
                .isVisible(request.isVisible() != null ? request.isVisible() : true)
                .build();

        portfolioRepository.save(portfolio);

        // Stacks
        if (request.stacks() != null) {
            for (PortfolioCreateRequest.PortfolioStackDto stackDto : request.stacks()) {
                Stack stack = stackRepository.findById(stackDto.stackId())
                        .orElseThrow(() -> new IllegalArgumentException("Stack not found: " + stackDto.stackId()));

                portfolioStackRepository.save(PortfolioStack.builder()
                        .portfolio(portfolio)
                        .stack(stack)
                        .expertLevel(stackDto.expertLevel())
                        .build());
            }
        }

        // URLs
        if (request.urls() != null) {
            for (PortfolioCreateRequest.PortfolioUrlDto urlDto : request.urls()) {
                portfolioUrlRepository.save(PortfolioUrl.builder()
                        .portfolio(portfolio)
                        .url(urlDto.url())
                        .build());
            }
        }

        // Images - temp에서 portfolio/{id}/로 이동
        if (request.images() != null && !request.images().isEmpty()) {
            try {
                List<String> tempUrls = request.images().stream()
                        .map(PortfolioCreateRequest.PortfolioImageDto::imageUrl)
                        .toList();
                String targetFolder = "portfolio/" + portfolio.getId();
                List<String> movedUrls = uploadService.moveFromTemp(tempUrls, targetFolder);

                for (int i = 0; i < request.images().size(); i++) {
                    portfolioImageRepository.save(PortfolioImage.builder()
                            .portfolio(portfolio)
                            .imageUrl(movedUrls.get(i))
                            .orders(request.images().get(i).orders())
                            .build());
                }
            } catch (IOException e) {
                log.error("Failed to move images for portfolio {}", portfolio.getId(), e);
            }
        }

        return portfolio.getId();
    }

    public PortfolioResponse getPortfolio(Long id) {
        // member fetch join으로 LAZY 로딩 문제 방지
        Portfolio portfolio = portfolioRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        SolvedAcInfo solvedAcInfo = fetchSolvedAcInfo(portfolio.getBojHandle());

        return PortfolioResponse.from(
                portfolio,
                portfolioStackRepository.findAllByPortfolioId(portfolio.getId()),
                portfolioUrlRepository.findAllByPortfolioId(portfolio.getId()),
                portfolioImageRepository.findAllByPortfolioId(portfolio.getId()),
                solvedAcInfo);
    }

    public List<PortfolioResponse> getMyPortfolios(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Portfolio> portfolios = portfolioRepository.findByMemberId(member.getId());

        return portfolios.stream().map(portfolio -> {
            SolvedAcInfo solvedAcInfo = fetchSolvedAcInfo(portfolio.getBojHandle());
            return PortfolioResponse.from(
                    portfolio,
                    portfolioStackRepository.findAllByPortfolioId(portfolio.getId()),
                    portfolioUrlRepository.findAllByPortfolioId(portfolio.getId()),
                    portfolioImageRepository.findAllByPortfolioId(portfolio.getId()),
                    solvedAcInfo);
        }).collect(Collectors.toList());
    }

    public List<PortfolioResponse> getAllPortfolios() {
        // member fetch join으로 N+1 문제 및 LAZY 로딩 문제 방지
        return portfolioRepository.findAllWithMember().stream().map(portfolio -> {
            SolvedAcInfo solvedAcInfo = fetchSolvedAcInfo(portfolio.getBojHandle());
            return PortfolioResponse.from(
                    portfolio,
                    portfolioStackRepository.findAllByPortfolioId(portfolio.getId()),
                    portfolioUrlRepository.findAllByPortfolioId(portfolio.getId()),
                    portfolioImageRepository.findAllByPortfolioId(portfolio.getId()),
                    solvedAcInfo);
        }).collect(Collectors.toList());
    }

    private SolvedAcInfo fetchSolvedAcInfo(String bojHandle) {
        if (bojHandle == null || bojHandle.isBlank()) {
            return null;
        }
        SolvedAcUserResponse response = solvedAcService.getUserInfo(bojHandle);
        return SolvedAcInfo.from(response);
    }

    public SolvedAcUserResponse verifySolvedAcHandle(String bojHandle) {
        return solvedAcService.getUserInfo(bojHandle);
    }

    @Transactional
    public Long updatePortfolio(Long id, PortfolioUpdateRequest request, String email) {
        // member fetch join으로 LAZY 로딩 문제 방지
        Portfolio portfolio = portfolioRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        if (!portfolio.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized: You are not the owner of this portfolio");
        }

        portfolio.update(
                request.title(),
                request.description(),
                request.introduction(),
                request.bojHandle(),
                request.swTestRank(),
                request.isVisible());

        // Delete existing relationships. Note: Ideally repositories should have these
        // methods.
        // Assuming implementation or will add them differently?
        // Wait, JpaRepository doesn't enforce deleteAllByPortfolioId if not defined.
        // I need to define them in Repo or iterate and delete.
        // For efficiency, let's use the list methods generated or delete manually.
        // Actually, `findAllByPortfolioId` returns list. I can delete them.

        List<PortfolioStack> oldStacks = portfolioStackRepository.findAllByPortfolioId(id);
        portfolioStackRepository.deleteAll(oldStacks);

        List<PortfolioUrl> oldUrls = portfolioUrlRepository.findAllByPortfolioId(id);
        portfolioUrlRepository.deleteAll(oldUrls);

        List<PortfolioImage> oldImages = portfolioImageRepository.findAllByPortfolioId(id);
        portfolioImageRepository.deleteAll(oldImages);
        // 기존 이미지 폴더 삭제
        try {
            uploadService.deleteFolder("portfolio/" + id);
        } catch (IOException e) {
            log.error("Failed to delete old images for portfolio {}", id, e);
        }

        // Re-inserting Stacks
        if (request.stacks() != null) {
            for (PortfolioCreateRequest.PortfolioStackDto stackDto : request.stacks()) {
                Stack stack = stackRepository.findById(stackDto.stackId())
                        .orElseThrow(() -> new IllegalArgumentException("Stack not found"));
                portfolioStackRepository.save(PortfolioStack.builder()
                        .portfolio(portfolio)
                        .stack(stack)
                        .expertLevel(stackDto.expertLevel())
                        .build());
            }
        }

        // Re-inserting URLs
        if (request.urls() != null) {
            for (PortfolioCreateRequest.PortfolioUrlDto urlDto : request.urls()) {
                portfolioUrlRepository.save(PortfolioUrl.builder()
                        .portfolio(portfolio)
                        .url(urlDto.url())
                        .build());
            }
        }

        // Re-inserting Images - temp에서 portfolio/{id}/로 이동
        if (request.images() != null && !request.images().isEmpty()) {
            try {
                List<String> tempUrls = request.images().stream()
                        .map(PortfolioCreateRequest.PortfolioImageDto::imageUrl)
                        .toList();
                String targetFolder = "portfolio/" + portfolio.getId();
                List<String> movedUrls = uploadService.moveFromTemp(tempUrls, targetFolder);

                for (int i = 0; i < request.images().size(); i++) {
                    portfolioImageRepository.save(PortfolioImage.builder()
                            .portfolio(portfolio)
                            .imageUrl(movedUrls.get(i))
                            .orders(request.images().get(i).orders())
                            .build());
                }
            } catch (IOException e) {
                log.error("Failed to move images for portfolio {}", portfolio.getId(), e);
            }
        }

        return portfolio.getId();
    }

    @Transactional
    public void deletePortfolio(Long id, String email) {
        // member fetch join으로 LAZY 로딩 문제 방지
        Portfolio portfolio = portfolioRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        if (!portfolio.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        // 이미지 폴더 삭제
        try {
            uploadService.deleteFolder("portfolio/" + id);
        } catch (IOException e) {
            log.error("Failed to delete images for portfolio {}", id, e);
        }

        // Hard delete for now
        portfolioRepository.delete(portfolio);
    }
}
