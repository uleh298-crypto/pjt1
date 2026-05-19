package com.ssafy.ssabree.core.repository.model

/**
 * 홈 화면 전체 데이터
 */
data class HomeModel(
    val dDays: List<DDayModel>,
    val team: RecruitThumbModel?,
    val study: RecruitThumbModel?,
    val campusMeals: List<CampusMealModel>,
    val boards: List<BoardThumbModel>
) {
    companion object {
        val sample = HomeModel(
            dDays = listOf(
                DDayModel("1차 프로젝트", 14),
                DDayModel("자격증 시험", 30)
            ),
            team = RecruitThumbModel(1, "팀 프로젝트", 3),
            study = RecruitThumbModel(2, "알고리즘 스터디", 4),
            campusMeals = listOf(
                CampusMealModel(1, "서울", listOf("url1", "url2")),
                CampusMealModel(2, "대전", listOf("url3"))
            ),
            boards = listOf(
                BoardThumbModel(1, "공지사항", "서버 점검 안내"),
                BoardThumbModel(2, "자유게시판", "오늘 점심 같이 드실 분?")
            )
        )
    }
}

data class DDayModel(
    val title: String,
    val days: Int
)

data class RecruitThumbModel(
    val id: Long,
    val name: String,
    val count: Int
)

data class BoardThumbModel(
    val id: Long,
    val name: String,
    val recentPostTitle: String?
)

data class CampusMealModel(
    val campusId: Long,
    val campusName: String,
    val imageUrls: List<String>
)
