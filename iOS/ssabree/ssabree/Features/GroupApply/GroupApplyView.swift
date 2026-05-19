import SwiftUI

struct GroupApplyView: View {
    let groupId: Int
    let groupKind: GroupKind
    @State var viewModel: GroupApplyViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var content: String = ""
    @State private var position: String = ""
    @State private var showSuccessDialog = false

    private var isSubmitEnabled: Bool {
        !title.isEmpty && !content.isEmpty && !position.isEmpty && viewModel.uiState.portfolioId != nil
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            topAppBar

            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Title Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("제목")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(AppColors.onBackground)

                        TextField("제목을 입력하세요", text: $title)
                            .textFieldStyle(ApplyTextFieldStyle())
                    }

                    // Content Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("상세 내용")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(AppColors.onBackground)

                        ZStack(alignment: .topLeading) {
                            if content.isEmpty {
                                Text("상세 내용을 입력하세요")
                                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
                                    .padding(.top, 12)
                                    .padding(.leading, 12)
                            }
                            TextEditor(text: $content)
                                .frame(minHeight: 250)
                                .padding(8)
                                .scrollContentBackground(.hidden)
                                .background(AppColors.surface.opacity(0.5))
                        }
                        .background(AppColors.surface.opacity(0.5))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    // Position Field
                    VStack(alignment: .leading, spacing: 8) {
                        Text("포지션")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(AppColors.onBackground)

                        TextField("예: BE", text: $position)
                            .textFieldStyle(ApplyTextFieldStyle())
                    }

                    // Portfolio Info Card
                    portfolioInfoCard

                    // Submit Button
                    HStack {
                        Spacer()
                        Button(action: {
                            Task {
                                await viewModel.applyGroup(
                                    groupId: groupId,
                                    groupKind: groupKind,
                                    title: title,
                                    message: content,
                                    position: position
                                )
                            }
                        }) {
                            Text("등록하기")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundStyle(.white)
                                .frame(width: 140, height: 50)
                                .background(isSubmitEnabled && !viewModel.uiState.isSubmitting ? AppColors.primary : Color.gray)
                                .clipShape(RoundedRectangle(cornerRadius: 25))
                        }
                        .disabled(!isSubmitEnabled || viewModel.uiState.isSubmitting)
                    }

                    Spacer().frame(height: 16)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadPortfolio()
        }
        .overlay {
            if viewModel.uiState.isSubmitting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
            }
        }
        .onChange(of: viewModel.uiState.isSuccess) { _, isSuccess in
            if isSuccess {
                showSuccessDialog = true
            }
        }
        .alert("알림", isPresented: $showSuccessDialog) {
            Button("확인") {
                viewModel.resetResult()
                dismiss()
            }
        } message: {
            Text("지원이 완료되었습니다.")
        }
        .alert("오류", isPresented: Binding(
            get: { viewModel.uiState.error != nil },
            set: { if !$0 { viewModel.uiState.error = nil } }
        )) {
            Button("확인", role: .cancel) {
                viewModel.resetResult()
            }
        } message: {
            if let error = viewModel.uiState.error {
                Text(error)
            }
        }
    }

    // MARK: - Top App Bar

    private var topAppBar: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("지원하기")
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

    // MARK: - Portfolio Info Card

    private var portfolioInfoCard: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                HStack(spacing: 12) {
                    Text("내 포트폴리오")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    if viewModel.uiState.isLoadingPortfolio {
                        ProgressView()
                            .scaleEffect(0.8)
                    }
                }

                Spacer()

                NavigationLink(value: AppRoute.portfolioDetail) {
                    Text("상세보기")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.primary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(AppColors.primary.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }

            Spacer().frame(height: 24)

            let summary = viewModel.uiState.portfolioSummary

            // Tech Stack
            PortfolioSectionView(icon: "chevron.left.forwardslash.chevron.right", label: "기술 스택") {
                if let techStack = summary?.techStack, !techStack.isEmpty {
                    let items = Array(techStack)
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        ForEach(items, id: \.key) { key, value in
                            Text("\(key) (\(toLevelLabel(value)))")
                                .font(.system(size: 12, weight: .medium))
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(AppColors.primaryContainer.opacity(0.3))
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                } else {
                    Text("등록된 기술이 없습니다.")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }
            }

            Spacer().frame(height: 20)

            // SW Rating
            PortfolioSectionView(icon: "brain.head.profile", label: "SW 역량", value: summary?.ssafySwRating ?? "-")

            Spacer().frame(height: 16)

            // Solved.ac
            PortfolioSectionView(icon: "trophy", label: "Solved.ac") {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("아이디: \(summary?.maskedSolvedAcHandle ?? "-")")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface)
                        Text("티어: \(summary?.solvedAcTierName ?? summary?.solvedAcRank ?? "-")")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface)
                        Text("푼 문제: \(summary?.solvedAcSolvedCount.map { "\($0)" } ?? "-")")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface)
                    }

                    Spacer()

                    if let tierImageUrl = summary?.solvedAcTierImageUrl, !tierImageUrl.isEmpty {
                        RemoteImageView(url: tierImageUrl, size: 28)
                    }
                }
            }

            Spacer().frame(height: 20)

            // Links
            PortfolioSectionView(icon: "link", label: "관련 링크 (블로그, 깃허브 등)") {
                if let links = summary?.links, !links.isEmpty {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(links, id: \.self) { url in
                            Text(url)
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.onSurface)
                        }
                    }
                } else {
                    Text("-")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }
            }

            Spacer().frame(height: 20)

            // Projects
            PortfolioSectionView(icon: "briefcase", label: "프로젝트 경험") {
                if let projects = summary?.projects, !projects.isEmpty {
                    VStack(alignment: .leading, spacing: 6) {
                        ForEach(Array(projects.enumerated()), id: \.offset) { index, title in
                            Text("\(index + 1). \(title)")
                                .font(.system(size: 13))
                                .foregroundStyle(AppColors.onSurface)
                        }
                    }
                } else {
                    Text("-")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }
            }
        }
        .padding(24)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.08), radius: 4, x: 0, y: 2)
    }

    private func toLevelLabel(_ raw: String?) -> String {
        switch raw?.trimmingCharacters(in: .whitespaces).lowercased() {
        case "high": return "상"
        case "mid": return "중"
        case "low": return "하"
        case "", nil: return "-"
        default: return raw ?? "-"
        }
    }
}

// MARK: - Portfolio Section View

private struct PortfolioSectionView<Content: View>: View {
    let icon: String
    let label: String
    let value: String?
    let content: Content?

    init(icon: String, label: String, value: String? = nil, @ViewBuilder content: () -> Content) {
        self.icon = icon
        self.label = label
        self.value = value
        self.content = content()
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundStyle(AppColors.primary)
                .frame(width: 20)

            VStack(alignment: .leading, spacing: 4) {
                Text(label)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                if let value = value {
                    Text(value)
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

extension PortfolioSectionView where Content == EmptyView {
    init(icon: String, label: String, value: String) {
        self.icon = icon
        self.label = label
        self.value = value
        self.content = nil
    }
}

// MARK: - Custom TextField Style

struct ApplyTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(12)
            .background(AppColors.surface.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
