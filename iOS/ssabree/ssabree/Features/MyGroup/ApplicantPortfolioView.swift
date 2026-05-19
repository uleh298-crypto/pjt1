import SwiftUI

// MARK: - UI State

struct ApplicantPortfolioUiState {
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var applicationTitle: String? = nil
    var applicationMessage: String? = nil
    var portfolio: PortfolioModel? = nil
    var projects: [ProjectModel] = []
}

// MARK: - ViewModel

@Observable
@MainActor
final class ApplicantPortfolioViewModel {
    private let portfolioRepository: PortfolioRepository
    private let projectRepository: ProjectRepository
    private let groupService: GroupService?

    var uiState = ApplicantPortfolioUiState()

    init(portfolioRepository: PortfolioRepository, projectRepository: ProjectRepository, groupService: GroupService? = nil) {
        self.portfolioRepository = portfolioRepository
        self.projectRepository = projectRepository
        self.groupService = groupService
    }

    func load(portfolioId: Int, applicationId: Int? = nil, groupKind: GroupKind? = nil) async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        // 지원서 정보와 포트폴리오 정보를 병렬로 로드
        async let applicationFetch: Void = loadApplicationDetail(applicationId: applicationId, groupKind: groupKind)
        async let portfolioResult = portfolioRepository.getPortfolio(id: portfolioId)

        _ = await applicationFetch
        let result = await portfolioResult

        switch result {
        case .success(let portfolio):
            uiState.portfolio = portfolio
            uiState.isLoading = false
            await loadProjects(id: portfolioId)
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    private func loadApplicationDetail(applicationId: Int?, groupKind: GroupKind?) async {
        guard let applicationId = applicationId, applicationId > 0,
              let groupKind = groupKind,
              let groupService = groupService else { return }

        do {
            let detail: GroupApplicationDetailResponse
            if groupKind == .study {
                detail = try await groupService.getStudyApplicationDetail(applicationId: applicationId)
            } else {
                detail = try await groupService.getTeamApplicationDetail(applicationId: applicationId)
            }
            uiState.applicationTitle = detail.title
            uiState.applicationMessage = detail.message
        } catch {
            print("[ApplicantPortfolioVM] loadApplicationDetail failed: \(error)")
        }
    }

    private func loadProjects(id: Int) async {
        let result = await portfolioRepository.getProjectsByPortfolio(portfolioId: id)

        switch result {
        case .success(let projects):
            uiState.projects = projects
        case .failure(let error):
            print("[ApplicantPortfolioVM] loadProjects failed: \(error)")
        }
    }
}

// MARK: - Summary View

struct ApplicantPortfolioView: View {
    let portfolioId: Int
    let applicationId: Int?
    let groupKind: GroupKind?
    @State var viewModel: ApplicantPortfolioViewModel
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL

    init(portfolioId: Int, applicationId: Int? = nil, groupKind: GroupKind? = nil, viewModel: ApplicantPortfolioViewModel) {
        self.portfolioId = portfolioId
        self.applicationId = applicationId
        self.groupKind = groupKind
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        VStack(spacing: 0) {
            headerView

            ScrollView {
                VStack(spacing: 16) {
                    if viewModel.uiState.isLoading {
                        ProgressView()
                            .padding(.vertical, 40)
                    } else if let error = viewModel.uiState.errorMessage {
                        Text(error)
                            .foregroundStyle(AppColors.error)
                            .font(.system(size: 13))
                    } else if viewModel.uiState.portfolio == nil {
                        Text("등록된 포트폴리오가 없습니다.")
                            .foregroundStyle(AppColors.onSurfaceVariant)
                            .font(.system(size: 13))
                            .padding(.top, 40)
                    } else {
                        // 지원서 정보 카드
                        if viewModel.uiState.applicationTitle != nil || viewModel.uiState.applicationMessage != nil {
                            applicationInfoCard
                        }
                        summaryCard
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.load(portfolioId: portfolioId, applicationId: applicationId, groupKind: groupKind)
        }
    }

    private var headerView: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(headerTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            // Placeholder for symmetry
            Image(systemName: "chevron.left")
                .font(.title3)
                .foregroundStyle(.clear)
        }
        .padding()
        .background(AppColors.background)
    }

    private var headerTitle: String {
        if let name = viewModel.uiState.portfolio?.memberName, !name.isEmpty {
            return "\(name)님의 지원서"
        }
        return "지원서"
    }

    private var applicationInfoCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            if let title = viewModel.uiState.applicationTitle, !title.isEmpty {
                HStack(spacing: 8) {
                    Image(systemName: "doc.text")
                        .font(.system(size: 16))
                        .foregroundStyle(AppColors.primary)
                    Text(title)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }
            }

            if let message = viewModel.uiState.applicationMessage, !message.isEmpty {
                Text(message)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
                    .padding(.leading, 24)
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
    }

    private var summaryCard: some View {
        let portfolio = viewModel.uiState.portfolio!
        let projects = viewModel.uiState.projects

        return VStack(alignment: .leading, spacing: 0) {
            // Header with title and detail button
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("포트폴리오 요약")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    Text(portfolio.memberName ?? "")
                        .font(.system(size: 13))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }

                Spacer()

                NavigationLink(value: AppRoute.applicantPortfolioDetail(portfolioId: portfolioId)) {
                    Text("상세보기")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.primary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(AppColors.primary.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }

            Spacer().frame(height: 20)

            // Tech Stacks
            SummaryRow(icon: "chevron.left.forwardslash.chevron.right", label: "기술 스택") {
                if portfolio.stacks.isEmpty {
                    Text("등록된 기술이 없습니다.")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                } else {
                    FlowLayout(spacing: 8) {
                        ForEach(portfolio.stacks, id: \.id) { stack in
                            HStack(spacing: 4) {
                                if let imgUrl = stack.stackImgUrl, !imgUrl.isEmpty {
                                    RemoteImageView(url: imgUrl, size: 16)
                                }
                                Text("\(stack.stackName) (\(stack.expertLevelLabel))")
                                    .font(.system(size: 12, weight: .medium))
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(AppColors.primaryContainer.opacity(0.3))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                }
            }

            Spacer().frame(height: 16)

            // SW Rating
            SummaryRow(icon: "brain.head.profile", label: "SW 역량", value: portfolio.swTestRank ?? "-")

            Spacer().frame(height: 16)

            // Solved.ac
            SummaryRow(icon: "trophy", label: "Solved.ac") {
                let tierName = portfolio.solvedAcInfo?.tierName ?? "-"
                let solvedCount = portfolio.solvedAcInfo?.solvedCount.map { "\($0)" } ?? "-"

                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("티어: \(tierName.isEmpty ? "-" : tierName)")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface)
                        Text("푼 문제: \(solvedCount)")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface)
                    }

                    Spacer()

                    if let tierImageUrl = portfolio.solvedAcInfo?.tierImageUrl, !tierImageUrl.isEmpty {
                        RemoteImageView(url: tierImageUrl, size: 28)
                    }
                }
            }

            Spacer().frame(height: 16)

            // Links
            SummaryRow(icon: "link", label: "관련 링크") {
                if portfolio.urls.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface)
                } else {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(portfolio.urls, id: \.id) { urlItem in
                            Text(urlItem.url.isEmpty ? "-" : urlItem.url)
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.primary)
                                .onTapGesture {
                                    openUrl(urlItem.url)
                                }
                        }
                    }
                }
            }

            Spacer().frame(height: 16)

            // Projects
            SummaryRow(icon: "briefcase", label: "프로젝트 경험") {
                if projects.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface)
                } else {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(Array(projects.enumerated()), id: \.element.id) { index, project in
                            Text("\(index + 1). \(project.title)")
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.onSurface)
                        }
                    }
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
    }

    private func openUrl(_ urlString: String) {
        let normalized = normalizeUrl(urlString)
        guard !normalized.isEmpty, let url = URL(string: normalized) else { return }
        openURL(url)
    }

    private func normalizeUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return "" }
        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }
        return "https://\(trimmed)"
    }
}

// MARK: - Summary Row

private struct SummaryRow<Content: View>: View {
    let icon: String
    let label: String
    let value: String?
    let content: Content?

    init(icon: String, label: String, value: String) where Content == EmptyView {
        self.icon = icon
        self.label = label
        self.value = value
        self.content = nil
    }

    init(icon: String, label: String, @ViewBuilder content: () -> Content) {
        self.icon = icon
        self.label = label
        self.value = nil
        self.content = content()
    }

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundStyle(AppColors.primary)
                .frame(width: 20)

            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(AppColors.onSurfaceVariant)

                if let value = value {
                    Text(value.isEmpty ? "-" : value)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }

                if let content = content {
                    content
                }
            }

            Spacer()
        }
    }
}

// MARK: - Detail View

struct ApplicantPortfolioDetailView: View {
    let portfolioId: Int
    @State var viewModel: ApplicantPortfolioViewModel
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @State private var previewImageUrl: String? = nil

    var body: some View {
        VStack(spacing: 0) {
            headerView

            ScrollView {
                VStack(spacing: 18) {
                    if viewModel.uiState.isLoading {
                        ProgressView()
                            .padding(.vertical, 40)
                    } else if let error = viewModel.uiState.errorMessage {
                        Text(error)
                            .foregroundStyle(AppColors.error)
                            .font(.system(size: 13))
                    } else if viewModel.uiState.portfolio == nil {
                        Text("등록된 포트폴리오가 없습니다.")
                            .foregroundStyle(AppColors.onSurfaceVariant)
                            .font(.system(size: 13))
                            .padding(.top, 40)
                    } else {
                        contentView
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.load(portfolioId: portfolioId)
        }
        .fullScreenCover(item: $previewImageUrl) { imageUrl in
            ImagePreviewView(imageUrl: imageUrl, onDismiss: { previewImageUrl = nil })
        }
    }

    private var headerView: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(headerTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Image(systemName: "chevron.left")
                .font(.title3)
                .foregroundStyle(.clear)
        }
        .padding()
        .background(AppColors.background)
    }

    private var headerTitle: String {
        if let name = viewModel.uiState.portfolio?.memberName, !name.isEmpty {
            return "\(name)님의 지원서"
        }
        return "지원서"
    }

    @ViewBuilder
    private var contentView: some View {
        let portfolio = viewModel.uiState.portfolio!

        // Title
        InfoCard {
            SectionView(title: "제목", icon: "person.text.rectangle") {
                Text(portfolio.title.isEmpty ? "-" : portfolio.title)
                    .font(.system(size: 15))
                    .foregroundStyle(AppColors.onSurface)
            }
        }

        // Description
        InfoCard {
            SectionView(title: "한 줄 소개", icon: "text.alignleft") {
                Text(portfolio.description.isEmpty ? "-" : portfolio.description)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
            }
        }

        // Introduction
        InfoCard {
            SectionView(title: "자기소개", icon: "person") {
                Text(portfolio.introduction.isEmpty ? "-" : portfolio.introduction)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
            }
        }

        // Tech Stacks
        InfoCard {
            SectionView(title: "기술 스택", icon: "chevron.left.forwardslash.chevron.right") {
                if portfolio.stacks.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface)
                } else {
                    FlowLayout(spacing: 8) {
                        ForEach(portfolio.stacks, id: \.id) { stack in
                            HStack(spacing: 4) {
                                if let imgUrl = stack.stackImgUrl, !imgUrl.isEmpty {
                                    RemoteImageView(url: imgUrl, size: 16)
                                }
                                Text("\(stack.stackName) (\(stack.expertLevelLabel))")
                                    .font(.system(size: 12))
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(AppColors.primaryContainer.opacity(0.3))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                }
            }
        }

        // SW & Solved.ac
        InfoCard {
            VStack(spacing: 16) {
                SectionView(title: "SW 역량", icon: "brain.head.profile") {
                    Text(portfolio.swTestRank ?? "-")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface)
                }

                SectionView(title: "Solved.ac 티어", icon: "trophy") {
                    let tierName = portfolio.solvedAcInfo?.tierName ?? "-"
                    let solvedCount = portfolio.solvedAcInfo?.solvedCount.map { "\($0)" } ?? "-"

                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("티어: \(tierName.isEmpty ? "-" : tierName)")
                                .font(.system(size: 12))
                            Text("푼 문제: \(solvedCount)")
                                .font(.system(size: 12))
                        }

                        Spacer()

                        if let tierImageUrl = portfolio.solvedAcInfo?.tierImageUrl, !tierImageUrl.isEmpty {
                            RemoteImageView(url: tierImageUrl, size: 28)
                        }
                    }
                }
            }
        }

        // URLs
        InfoCard {
            SectionView(title: "관련 링크", icon: "link") {
                if portfolio.urls.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface)
                } else {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(portfolio.urls, id: \.id) { urlItem in
                            Text(urlItem.url.isEmpty ? "-" : urlItem.url)
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.primary)
                                .onTapGesture {
                                    openUrl(urlItem.url)
                                }
                        }
                    }
                }
            }
        }

        // Projects
        SectionView(title: "프로젝트 경험", icon: "briefcase") {
            if viewModel.uiState.projects.isEmpty {
                Text("-")
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
            } else {
                VStack(spacing: 12) {
                    ForEach(viewModel.uiState.projects) { project in
                        ProjectExperienceCard(
                            project: project,
                            onOpenUrl: { openUrl($0) },
                            onImageClick: { previewImageUrl = $0 }
                        )
                    }
                }
            }
        }
    }

    private func openUrl(_ urlString: String) {
        let normalized = normalizeUrl(urlString)
        guard !normalized.isEmpty, let url = URL(string: normalized) else { return }
        openURL(url)
    }

    private func normalizeUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return "" }
        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }
        return "https://\(trimmed)"
    }
}

// MARK: - Info Card

private struct InfoCard<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading) {
            content
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 2, x: 0, y: 1)
    }
}

// MARK: - Section View

private struct SectionView<Content: View>: View {
    let title: String
    let icon: String
    let content: Content

    init(title: String, icon: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.icon = icon
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 16))
                    .foregroundStyle(AppColors.primary)
                    .frame(width: 20)

                Text(title)
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer().frame(height: 12)

            HStack {
                Spacer().frame(width: 28)
                content
            }
        }
    }
}

// MARK: - Project Experience Card

private struct ProjectExperienceCard: View {
    let project: ProjectModel
    let onOpenUrl: (String) -> Void
    let onImageClick: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // Title Header
            HStack(spacing: 8) {
                Image(systemName: "briefcase")
                    .font(.system(size: 16))
                    .foregroundStyle(AppColors.primary)
                    .frame(width: 20)

                Text("제목")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
            }

            Text(project.title.isEmpty ? "-" : project.title)
                .font(.system(size: 15))
                .foregroundStyle(AppColors.onSurface)
                .padding(.leading, 28)

            // Introduction
            ProjectField(icon: "text.alignleft", label: "한 줄 소개", value: project.introduction ?? "-")

            // Description
            ProjectField(icon: "doc.text", label: "상세 내용", value: project.description ?? "-")

            // Tech Stacks
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: "wrench.and.screwdriver")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.primary)
                        .frame(width: 18)

                    Text("사용된 기술 스택")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }

                if project.techStacks.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .padding(.leading, 26)
                } else {
                    FlowLayout(spacing: 8) {
                        ForEach(project.techStacks, id: \.self) { stack in
                            Text(stack)
                                .font(.system(size: 12))
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(AppColors.surfaceVariant.opacity(0.5))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                    .padding(.leading, 26)
                }
            }

            // URLs
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: "link")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.primary)
                        .frame(width: 18)

                    Text("링크")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }

                if project.urls.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .padding(.leading, 26)
                } else {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(project.urls, id: \.self) { url in
                            Text(url.isEmpty ? "-" : url)
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.primary)
                                .onTapGesture { onOpenUrl(url) }
                        }
                    }
                    .padding(.leading, 26)
                }
            }

            // Images
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: "photo")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.primary)
                        .frame(width: 18)

                    Text("이미지")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }

                let filteredImages = project.imageUrls.filter { !$0.isEmpty }
                if filteredImages.isEmpty {
                    Text("-")
                        .font(.system(size: 14))
                        .padding(.leading, 26)
                } else {
                    ImageCarousel(
                        imageUrls: filteredImages,
                        onImageClick: onImageClick
                    )
                    .padding(.leading, 26)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 2, x: 0, y: 1)
    }
}

// MARK: - Project Field

private struct ProjectField: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundStyle(AppColors.primary)
                .frame(width: 18)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
                Text(value.isEmpty ? "-" : value)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
            }
        }
    }
}

// MARK: - Image Carousel

private struct ImageCarousel: View {
    let imageUrls: [String]
    let onImageClick: (String) -> Void

    var body: some View {
        VStack(spacing: 8) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    ForEach(imageUrls, id: \.self) { imageUrl in
                        CachedAsyncImage(url: normalizeImageUrl(imageUrl)) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Rectangle()
                                .fill(AppColors.surfaceVariant)
                        }
                        .frame(width: 220, height: 150)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .onTapGesture {
                            onImageClick(normalizeImageUrl(imageUrl))
                        }
                    }
                }
            }

            if imageUrls.count > 1 {
                HStack(spacing: 6) {
                    ForEach(0..<imageUrls.count, id: \.self) { index in
                        Circle()
                            .fill(index == 0 ? AppColors.primary : AppColors.onSurface.opacity(0.3))
                            .frame(width: 6, height: 6)
                    }
                }
            }
        }
    }

    private func normalizeImageUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return "" }
        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }
        let normalized = trimmed.replacingOccurrences(of: "\\", with: "/")
        if normalized.contains("/uploads/") {
            if let range = normalized.range(of: "/uploads/") {
                let relative = String(normalized[range.lowerBound...])
                return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + relative
            }
        }
        return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/uploads/" + normalized.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }
}

// MARK: - Image Preview View

private struct ImagePreviewView: View {
    let imageUrl: String
    let onDismiss: () -> Void
    @State private var scale: CGFloat = 1.0

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            CachedAsyncImage(url: imageUrl) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .scaleEffect(scale)
                    .gesture(
                        MagnificationGesture()
                            .onChanged { value in
                                scale = min(max(value, 1), 3)
                            }
                    )
            } placeholder: {
                ProgressView()
                    .tint(.white)
            }

            VStack {
                HStack {
                    Spacer()
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.title2)
                            .foregroundStyle(.white)
                            .padding()
                    }
                }
                Spacer()
            }
        }
        .onTapGesture(perform: onDismiss)
    }
}

// MARK: - Flow Layout

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y), proposal: .unspecified)
        }
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, positions: [CGPoint]) {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }
            positions.append(CGPoint(x: currentX, y: currentY))
            lineHeight = max(lineHeight, size.height)
            currentX += size.width + spacing
            maxX = max(maxX, currentX - spacing)
        }

        return (CGSize(width: maxX, height: currentY + lineHeight), positions)
    }
}
