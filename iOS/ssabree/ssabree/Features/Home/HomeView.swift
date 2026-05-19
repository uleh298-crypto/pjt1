import SwiftUI

// MARK: - Android Compose → SwiftUI 대응
// PullToRefreshBox → .refreshable modifier
// HorizontalPager → TabView with .tabViewStyle(.page)
// LazyRow → ScrollView(.horizontal) + HStack
// Card(RoundedCornerShape, elevation) → background + clipShape + shadow
// FilterChip → Custom chip button
// AsyncImage → AsyncImage (iOS 15+)
// LinearGradient brush → LinearGradient

// MARK: - Banner Model
private struct HomeBanner: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let gradientColors: [Color]
    let icon: String // SF Symbol name
    let onTap: () -> Void
}

// MARK: - HomeView

struct HomeView: View {
    var viewModel: HomeViewModel  // @State 제거 - 외부에서 주입받음
    var onNotificationTap: () -> Void = {}
    var onMyPageTap: () -> Void = {}
    var onDdayTap: () -> Void = {}
    var onProjectTap: () -> Void = {}
    var onStudyTap: () -> Void = {}
    var onBoardTap: (Int) -> Void = { _ in }

    init(
        viewModel: HomeViewModel,
        onNotificationTap: @escaping () -> Void = {},
        onMyPageTap: @escaping () -> Void = {},
        onDdayTap: @escaping () -> Void = {},
        onProjectTap: @escaping () -> Void = {},
        onStudyTap: @escaping () -> Void = {},
        onBoardTap: @escaping (Int) -> Void = { _ in }
    ) {
        self.viewModel = viewModel
        self.onNotificationTap = onNotificationTap
        self.onMyPageTap = onMyPageTap
        self.onDdayTap = onDdayTap
        self.onProjectTap = onProjectTap
        self.onStudyTap = onStudyTap
        self.onBoardTap = onBoardTap
    }

    var body: some View {
        // Android: Box(modifier = Modifier.fillMaxSize().background(background))
        ZStack {
            AppColors.background
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Android: TopBar
                HomeTopBar(
                    onNotificationTap: onNotificationTap,
                    onMyPageTap: onMyPageTap
                )

                // Android: PullToRefreshBox → .refreshable
                ScrollView {
                    // Android: Column(padding(horizontal = 20.dp, vertical = 12.dp))
                    VStack(spacing: 0) {
                        // 인사 영역 + D-Day
                        greetingSection

                        Spacer().frame(height: 16)

                        // 배너 영역 (프로젝트, 스터디)
                        bannerSection

                        Spacer().frame(height: 24)

                        // 점심 메뉴 섹션
                        lunchSection

                        Spacer().frame(height: 16)

                        // 전체 게시판 섹션
                        boardSection

                        Spacer().frame(height: 20)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                }
                .refreshable {
                    await viewModel.load()
                }
                .task {
                    await viewModel.loadInitialDataIfNeeded()
                }
            }

            // 로딩 표시
            if viewModel.uiState.isLoading {
                Color.black.opacity(0.1)
                    .ignoresSafeArea()
                ProgressView()
            }

            // 확대된 점심 이미지 오버레이
            EnlargedMealImageOverlay(
                imageUrl: viewModel.uiState.enlargedMealImageUrl,
                onDismiss: { viewModel.onMealImageDismiss() }
            )
        }
    }

    // MARK: - Greeting Section
    // Android: Row with greeting text + D-Day HorizontalPager

    private var greetingSection: some View {
        HStack(alignment: .center) {
            // 인사말
            VStack(alignment: .leading, spacing: 4) {
                // Android: Text("싸용자님, 안녕하세요!", fontSize = 20.sp, fontWeight = Bold)
                Text("싸용자님, 안녕하세요!")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                // Android: Text("오늘도 싸브리타임과 함께 힘내요.", bodySmall, alpha = 0.7f)
                Text("오늘도 싸브리타임과 함께 힘내요.")
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onBackground.opacity(0.7))
            }

            Spacer()

            // D-Day 슬라이더 배지
            DdaySliderBadge(
                dDays: viewModel.uiState.home.dDays,
                onTap: onDdayTap
            )
        }
    }

    // MARK: - Banner Section
    // Android: Row with two HomeGradientCard

    private var bannerSection: some View {
        let bannerItems = createBannerItems()

        return HStack(spacing: 10) {
            ForEach(bannerItems) { banner in
                HomeGradientCard(banner: banner)
            }
        }
    }

    private func createBannerItems() -> [HomeBanner] {
        let team = viewModel.uiState.home.team
        let study = viewModel.uiState.home.study

        let teamSubtitle = team.map { t in
            "\(t.name ?? "프로젝트")\n\(t.count)명 모집 중"
        } ?? "모집 중인 팀이 없습니다"

        let studySubtitle = study.map { s in
            "\(s.name ?? "스터디")\n\(s.count)명 모집 중"
        } ?? "모집 중인 스터디가 없습니다"

        return [
            HomeBanner(
                title: "프로젝트",
                subtitle: teamSubtitle,
                gradientColors: [
                    Color(red: 0x9F/255, green: 0xD7/255, blue: 0xFF/255),
                    Color(red: 0xB5/255, green: 0x9C/255, blue: 0xFF/255)
                ],
                icon: "person.3.fill",
                onTap: onProjectTap
            ),
            HomeBanner(
                title: "스터디",
                subtitle: studySubtitle,
                gradientColors: [
                    Color(red: 0x8F/255, green: 0xD5/255, blue: 0xFF/255),
                    Color(red: 0x6B/255, green: 0xA9/255, blue: 0xFF/255)
                ],
                icon: "book.fill",
                onTap: onStudyTap
            )
        ]
    }

    // MARK: - Lunch Section

    private var lunchSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Android: Text("점심 메뉴", titleMedium)
            Text("점심 메뉴")
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(AppColors.onBackground)

            LunchCard(
                locationOptions: viewModel.uiState.locationOptions,
                selectedLocation: viewModel.uiState.selectedLocation,
                onLocationSelected: { viewModel.selectLocation($0) },
                mealImageUrls: viewModel.uiState.selectedMealImageUrls,
                onMealImageClick: { viewModel.onMealImageClick($0) }
            )
        }
    }

    // MARK: - Board Section

    private var boardSection: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Android: Text("전체 게시판", titleMedium)
            Text("전체 게시판")
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(AppColors.onSurface)

            BoardListCard(
                boards: viewModel.uiState.home.boards,
                onBoardTap: onBoardTap
            )
        }
    }
}

// MARK: - Home Top Bar
// Android: Surface(color = background, tonalElevation = 2.dp) { Box(height = 60.dp) }

private struct HomeTopBar: View {
    var onNotificationTap: () -> Void
    var onMyPageTap: () -> Void

    var body: some View {
        ZStack {
            // Logo (left)
            HStack {
                Image("home_logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 140)
                    .padding(.leading, 16)
                Spacer()
            }

            // Icons (right)
            HStack {
                Spacer()
                Button(action: onNotificationTap) {
                    Image(systemName: "bell.fill")
                        .font(.system(size: 20))
                        .foregroundStyle(AppColors.onSurface)
                        .frame(width: 44, height: 44)
                }
                Button(action: onMyPageTap) {
                    Image(systemName: "person.circle.fill")
                        .font(.system(size: 20))
                        .foregroundStyle(AppColors.onSurface)
                        .frame(width: 44, height: 44)
                }
            }
        }
        .frame(height: 60)
        .background(AppColors.background)
    }
}

// MARK: - Gradient Card
// Android: Card(RoundedCornerShape(14.dp), elevation = 3.dp) with LinearGradient

private struct HomeGradientCard: View {
    let banner: HomeBanner

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Title row
            HStack {
                Text(banner.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(.white)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(.white.opacity(0.95))
            }

            Spacer().frame(height: 4)

            // Subtitle
            Text(banner.subtitle)
                .font(.system(size: 11))
                .foregroundStyle(.white.opacity(0.92))
                .lineSpacing(2)

            Spacer().frame(height: 8)

            // Icon (right aligned)
            HStack {
                Spacer()
                Image(systemName: banner.icon)
                    .font(.system(size: 32))
                    .foregroundStyle(.white)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                colors: banner.gradientColors,
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.1), radius: 3, x: 0, y: 2)
        .onTapGesture { banner.onTap() }
    }
}

// MARK: - Lunch Card
// Android: Card(RoundedCornerShape(20.dp), elevation = 2.dp)

private struct LunchCard: View {
    let locationOptions: [String]
    let selectedLocation: String
    let onLocationSelected: (String) -> Void
    let mealImageUrls: [String]
    var onMealImageClick: (String) -> Void = { _ in }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 캠퍼스 라벨
            Text("캠퍼스")
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(AppColors.onSurface.opacity(0.7))
                .padding(.bottom, 4)

            // 캠퍼스 선택 칩
            // Android: LazyRow with FilterChip
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(locationOptions, id: \.self) { option in
                        LocationChip(
                            title: option,
                            isSelected: selectedLocation == option,
                            action: { onLocationSelected(option) }
                        )
                    }
                }
            }

            Spacer().frame(height: 16)

            // 선택된 캠퍼스 정보
            Text("\(selectedLocation) 캠퍼스 오늘의 점심")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(AppColors.onSurface)

            Spacer().frame(height: 4)

            Text("표시된 메뉴는 실제 식단과 다를 수 있습니다.")
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onSurface.opacity(0.6))

            Spacer().frame(height: 12)

            // 점심 이미지
            if mealImageUrls.isEmpty {
                // 이미지 없음
                RoundedRectangle(cornerRadius: 12)
                    .fill(AppColors.background)
                    .frame(height: 100)
                    .overlay(
                        Text("등록된 점심 이미지가 없습니다.")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    )
            } else {
                // 2열 그리드
                MealImageGrid(imageUrls: mealImageUrls, onImageClick: onMealImageClick)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

private struct LocationChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(isSelected ? AppColors.onPrimary : AppColors.onSurface)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(isSelected ? AppColors.primary : AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(
                            isSelected ? Color.clear : AppColors.onSurface.opacity(0.3),
                            lineWidth: 1
                        )
                )
        }
        .buttonStyle(.plain)
    }
}

private struct MealImageGrid: View {
    let imageUrls: [String]
    var onImageClick: (String) -> Void = { _ in }

    var body: some View {
        let chunkedUrls = imageUrls.chunked(into: 2)

        VStack(spacing: 8) {
            ForEach(Array(chunkedUrls.enumerated()), id: \.offset) { _, rowUrls in
                HStack(alignment: .top, spacing: 8) {
                    ForEach(rowUrls, id: \.self) { url in
                        CachedAsyncImage(url: url) { image in
                            image
                                .resizable()
                                .scaledToFit()
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        } placeholder: {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(AppColors.background)
                                .frame(height: 120)
                                .overlay(ProgressView())
                        }
                        .frame(maxWidth: .infinity)
                        .onTapGesture { onImageClick(url) }
                    }
                    // 홀수 개일 경우 빈 공간
                    if rowUrls.count == 1 {
                        Color.clear.frame(maxWidth: .infinity)
                    }
                }
            }
        }
    }
}

// MARK: - Board List Card
// Android: Card(RoundedCornerShape(18.dp), elevation = 2.dp)

private struct BoardListCard: View {
    let boards: [HomeBoardModel]
    let onBoardTap: (Int) -> Void

    var body: some View {
        VStack(spacing: 0) {
            if boards.isEmpty {
                BoardListItem(
                    title: "게시판 목록이 없습니다",
                    subtitle: "최근 게시물 없음",
                    showDivider: false
                )
            } else {
                ForEach(Array(boards.enumerated()), id: \.element.id) { index, board in
                    BoardListItem(
                        title: board.name,
                        subtitle: board.recentPostTitle ?? "최근 게시물이 없습니다.",
                        showDivider: index != boards.count - 1
                    )
                    .onTapGesture { onBoardTap(board.id) }
                }
            }
        }
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

private struct BoardListItem: View {
    let title: String
    let subtitle: String
    let showDivider: Bool

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 10) {
                // Dot indicator
                Circle()
                    .fill(AppColors.primary)
                    .frame(width: 6, height: 6)
                    .padding(.top, 6)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(AppColors.onSurface)

                    Text(subtitle)
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurface.opacity(0.75))
                        .lineLimit(1)
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            if showDivider {
                Divider()
                    .background(AppColors.onSurface.opacity(0.1))
            }
        }
    }
}

// MARK: - HomeViewModel & UiState

struct HomeUiState {
    var isLoading: Bool = false
    var home: HomeModel = .sample
    var errorMessage: String?
    var selectedLocation: String = ""
    var locationOptions: [String] = ["서울", "대전", "광주", "구미", "부울경"]
    var selectedMealImageUrls: [String] = []
    var enlargedMealImageUrl: String? = nil
}

@Observable
final class HomeViewModel {
    private let homeRepository: HomeRepository
    private let myPageRepository: MyPageRepository
    var uiState = HomeUiState()
    private var hasLoadedInitialData = false

    init(homeRepository: HomeRepository, myPageRepository: MyPageRepository) {
        self.homeRepository = homeRepository
        self.myPageRepository = myPageRepository
        // init에서 Task 제거 - View의 .task modifier에서 loadInitialDataIfNeeded 호출
    }

    /// View의 .task modifier에서 호출 - 최초 1회만 데이터 로드
    /// Task.detached를 사용하여 SwiftUI의 .task cancellation으로부터 보호
    @MainActor
    func loadInitialDataIfNeeded() async {
        guard !hasLoadedInitialData else { return }
        hasLoadedInitialData = true

        // Task.detached: 부모 Task의 cancellation을 상속받지 않음
        await Task.detached { @MainActor [self] in
            await self.load()
        }.value
    }

    @MainActor
    func load() async {
        uiState.isLoading = true
        // 사용자 캠퍼스 선호값 (실패해도 무시)
        var userCampus: String? = nil
        let myPageResult = await myPageRepository.getMyPage()
        if case .success(let page) = myPageResult {
            userCampus = page.user?.campus
        }

        let result = await homeRepository.fetchHome()
        switch result {
        case .success(let model):
            uiState.home = model
            uiState.errorMessage = nil
            // 캠퍼스 목록 업데이트
            let campusNames = model.lunchMenu.map { $0.campusName }
            if !campusNames.isEmpty {
                uiState.locationOptions = Array(Set(campusNames)).sorted()
                if let userCampus, uiState.locationOptions.contains(userCampus) {
                    uiState.selectedLocation = userCampus
                } else if uiState.selectedLocation.isEmpty || !uiState.locationOptions.contains(uiState.selectedLocation) {
                    uiState.selectedLocation = uiState.locationOptions.first ?? "서울"
                }
            }
            updateMealImages()
        case .failure:
            uiState.errorMessage = "홈 데이터를 불러오지 못했습니다."
        }
        uiState.isLoading = false
    }

    func selectLocation(_ location: String) {
        uiState.selectedLocation = location
        updateMealImages()
    }

    private func updateMealImages() {
        let meals = uiState.home.lunchMenu.filter { $0.campusName == uiState.selectedLocation }
        uiState.selectedMealImageUrls = meals.flatMap { $0.imageUrls }
    }

    func onMealImageClick(_ imageUrl: String) {
        uiState.enlargedMealImageUrl = imageUrl
    }

    func onMealImageDismiss() {
        uiState.enlargedMealImageUrl = nil
    }
}

// MARK: - D-Day Slider Badge
// Android: HorizontalPager with auto-scroll every 5 seconds

private struct DdaySliderBadge: View {
    let dDays: [DDayModel]
    let onTap: () -> Void

    @State private var currentIndex = 0
    @State private var timer: Timer?

    private var dDayItems: [String] {
        let items = dDays.map { dDay in
            if dDay.title.isEmpty {
                return "D-\(dDay.days)"
            }
            return "\(dDay.title) D-\(dDay.days)"
        }
        return items.isEmpty ? ["D-0"] : items
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: "calendar")
                    .font(.system(size: 16))
                    .foregroundStyle(Color(red: 0x5B/255, green: 0x7F/255, blue: 0xFF/255))

                // 슬라이더 텍스트
                Text(dDayItems[currentIndex])
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(Color(red: 0x5B/255, green: 0x7F/255, blue: 0xFF/255))
                    .animation(.easeInOut(duration: 0.3), value: currentIndex)
                    .id(currentIndex) // 애니메이션 트리거
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color(red: 0xEE/255, green: 0xF2/255, blue: 0xFB/255))
            .clipShape(RoundedRectangle(cornerRadius: 24))
        }
        .buttonStyle(.plain)
        .onAppear {
            startTimer()
        }
        .onDisappear {
            stopTimer()
        }
        .onChange(of: dDays.count) { _, _ in
            currentIndex = 0
        }
    }

    private func startTimer() {
        guard dDayItems.count > 1 else { return }

        timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { _ in
            withAnimation(.easeInOut(duration: 0.5)) {
                currentIndex = (currentIndex + 1) % dDayItems.count
            }
        }
    }

    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
}

// MARK: - Enlarged Meal Image Overlay

private struct EnlargedMealImageOverlay: View {
    let imageUrl: String?
    let onDismiss: () -> Void

    var body: some View {
        if let imageUrl = imageUrl {
            ZStack {
                // 반투명 배경
                Color.black.opacity(0.7)
                    .ignoresSafeArea()
                    .onTapGesture { onDismiss() }

                // 확대된 이미지
                CachedAsyncImage(url: imageUrl) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                } placeholder: {
                    ProgressView()
                        .tint(.white)
                }
                .frame(maxWidth: UIScreen.main.bounds.width * 0.95)
                .frame(maxHeight: UIScreen.main.bounds.height * 0.85)
                .onTapGesture { onDismiss() }
            }
            .transition(.opacity)
            .animation(.easeInOut(duration: 0.2), value: imageUrl)
        }
    }
}

// MARK: - Array Extension

private extension Array {
    func chunked(into size: Int) -> [[Element]] {
        stride(from: 0, to: count, by: size).map {
            Array(self[$0..<Swift.min($0 + size, count)])
        }
    }
}
