package com.ssafy.ssabre.home.service;

import com.ssafy.ssabre.board.entity.Board;
import com.ssafy.ssabre.board.repository.BoardRepository;
import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.entity.Menu;
import com.ssafy.ssabre.campus.repository.CampusRepository;
import com.ssafy.ssabre.campus.repository.EnrollmentRepository;
import com.ssafy.ssabre.campus.repository.MenuRepository;
import com.ssafy.ssabre.home.dto.*;
import com.ssafy.ssabre.member.entity.DDay;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.DDayRepository;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.repository.PostRepository;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.repository.StudyRepository;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final MemberRepository memberRepository;
    private final DDayRepository dDayRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeamRepository teamRepository;
    private final StudyRepository studyRepository;
    private final MenuRepository menuRepository;
    private final CampusRepository campusRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    public HomeResponse getHome(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Long campusId = getCampusId(member.getId());

        List<DDayItem> dDays = getDDays(member.getId());
        ThumbnailItem teamThumbnail = getTeamThumbnail(campusId);
        ThumbnailItem studyThumbnail = getStudyThumbnail(campusId);
        List<CampusMealItem> campusMeals = getAllCampusMeals();
        List<BoardItem> boardsList = getBoardsList();

        return new HomeResponse(dDays, teamThumbnail, studyThumbnail, campusMeals, boardsList);
    }

    private Long getCampusId(Long memberId) {
        return enrollmentRepository.findByMember_IdAndDeletedAtIsNull(memberId)
                .map(enrollment -> enrollment.getClasses().getCampus().getId())
                .orElse(null);
    }

    private List<DDayItem> getDDays(Long memberId) {
        List<DDay> dDays = dDayRepository.findByMemberIdAndDeletedAtIsNullOrderByTargetDateAsc(memberId);
        LocalDate today = LocalDate.now();

        return dDays.stream()
                .map(dDay -> new DDayItem(
                        dDay.getTitle(),
                        ChronoUnit.DAYS.between(today, dDay.getTargetDate())
                ))
                .toList();
    }

    private ThumbnailItem getTeamThumbnail(Long campusId) {
        if (campusId == null) {
            return new ThumbnailItem(null, 0);
        }

        Optional<Team> team = teamRepository.findLatestOpenTeamByCampusId(campusId);
        return team.map(t -> new ThumbnailItem(t.getTitle(), 1))
                .orElse(new ThumbnailItem(null, 0));
    }

    private ThumbnailItem getStudyThumbnail(Long campusId) {
        if (campusId == null) {
            return new ThumbnailItem(null, 0);
        }

        Optional<Study> study = studyRepository.findLatestOpenStudyByCampusId(campusId);
        return study.map(s -> new ThumbnailItem(s.getTitle(), 1))
                .orElse(new ThumbnailItem(null, 0));
    }

    private List<CampusMealItem> getAllCampusMeals() {
        LocalDate today = LocalDate.now();
        List<Campus> campuses = campusRepository.findAll();

        return campuses.stream()
                .map(campus -> {
                    List<Menu> menus = menuRepository.findByCampusIdAndDateAndDeletedAtIsNull(campus.getId(), today);
                    List<String> imageUrls = menus.stream()
                            .map(Menu::getImageUrl)
                            .filter(url -> url != null && !url.isBlank())
                            .toList();
                    return new CampusMealItem(campus.getId(), campus.getName(), imageUrls);
                })
                .toList();
    }

    private List<BoardItem> getBoardsList() {
        List<Board> boards = boardRepository.findByDeletedAtIsNull();
        List<BoardItem> boardItems = new ArrayList<>();

        for (Board board : boards) {
            Optional<Post> recentPost = postRepository.findLatestVisiblePost(board.getId());
            String recentPostTitle = recentPost.map(Post::getTitle).orElse(null);
            boardItems.add(new BoardItem(board.getId(), board.getName(), recentPostTitle));
        }

        return boardItems;
    }
}
