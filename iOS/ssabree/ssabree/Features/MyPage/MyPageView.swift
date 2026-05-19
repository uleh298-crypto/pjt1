import SwiftUI
import PhotosUI
import UIKit

struct MyPageView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: MyPageViewModel
    let authRepository: AuthRepository
    let onLogout: () -> Void
    let onSettingTap: () -> Void
    let onPortfolioDetailTap: () -> Void
    let onMyPostsTap: () -> Void
    let onMyCommentsTap: () -> Void
    let onMyScrapsTap: () -> Void

    @State private var showLogoutAlert = false
    @State private var isLoggingOut = false
    @State private var showImagePicker = false
    @State private var showFullScreenImage = false
    @State private var selectedPhotoItem: PhotosPickerItem? = nil

    init(
        viewModel: MyPageViewModel,
        authRepository: AuthRepository,
        onLogout: @escaping () -> Void,
        onSettingTap: @escaping () -> Void = {},
        onPortfolioDetailTap: @escaping () -> Void = {},
        onMyPostsTap: @escaping () -> Void = {},
        onMyCommentsTap: @escaping () -> Void = {},
        onMyScrapsTap: @escaping () -> Void = {}
    ) {
        self._viewModel = State(initialValue: viewModel)
        self.authRepository = authRepository
        self.onLogout = onLogout
        self.onSettingTap = onSettingTap
        self.onPortfolioDetailTap = onPortfolioDetailTap
        self.onMyPostsTap = onMyPostsTap
        self.onMyCommentsTap = onMyCommentsTap
        self.onMyScrapsTap = onMyScrapsTap
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }

                Spacer()

                Text("마이페이지")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                Spacer()

                Button(action: onSettingTap) {
                    Image(systemName: "gearshape")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            }
            .padding()
            .background(AppColors.background)

            ScrollView {
                VStack(spacing: 0) {
                    Spacer().frame(height: 16)

                    // Loading / Error
                    if viewModel.uiState.isLoading {
                        ProgressView()
                            .padding(.vertical, 12)
                    } else if let error = viewModel.uiState.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(AppColors.error)
                    }

                    // Profile Header
                    ProfileHeaderView(
                        user: viewModel.uiState.myPage?.user,
                        isUploadingImage: viewModel.uiState.isUploadingImage,
                        onImageTap: {
                            if viewModel.uiState.myPage?.user?.profileImageUrl != nil {
                                showFullScreenImage = true
                            }
                        },
                        onCameraTap: { showImagePicker = true }
                    )
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 24)

                    // Stats Row
                    StatsRowView(
                        counts: viewModel.uiState.myPage?.counts,
                        onPostsTap: onMyPostsTap,
                        onCommentsTap: onMyCommentsTap,
                        onScrapsTap: onMyScrapsTap
                    )
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 28)

                    Divider()
                        .background(AppColors.onSurface.opacity(0.2))
                        .padding(.horizontal, 20)

                    Spacer().frame(height: 28)

                    // Portfolio Info Card
                    PortfolioInfoCard(
                        summary: viewModel.uiState.myPage?.portfolioSummary,
                        isLoading: viewModel.uiState.isLoadingPortfolio,
                        stackImageMap: viewModel.uiState.stackImageMap,
                        onDetailTap: onPortfolioDetailTap
                    )
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 48)

                    // Logout
                    Button(action: { showLogoutAlert = true }) {
                        Text("로그아웃")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(AppColors.onSurface.opacity(0.7))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                    }
                    .disabled(isLoggingOut)
                    .padding(.horizontal, 24)

                    Spacer().frame(height: 24)
                }
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadMyPage()
        }
        .alert("로그아웃", isPresented: $showLogoutAlert) {
            Button("취소", role: .cancel) {}
            Button("로그아웃", role: .destructive) {
                performLogout()
            }
        } message: {
            Text("정말 로그아웃 하시겠습니까?")
        }
        .photosPicker(isPresented: $showImagePicker, selection: $selectedPhotoItem, matching: .images)
        .onChange(of: selectedPhotoItem) { _, newItem in
            handlePhotoSelection(newItem)
        }
        .fullScreenCover(isPresented: $showFullScreenImage) {
            if let imageUrl = viewModel.uiState.myPage?.user?.profileImageUrl {
                FullScreenImageView(imageUrl: imageUrl, onDismiss: { showFullScreenImage = false })
            }
        }
    }

    private func performLogout() {
        isLoggingOut = true
        Task {
            let result = await authRepository.logout()
            await MainActor.run {
                isLoggingOut = false
                switch result {
                case .success:
                    onLogout()
                case .failure(let error):
                    print("Logout failed: \(error)")
                    onLogout()
                }
            }
        }
    }

    private func handlePhotoSelection(_ item: PhotosPickerItem?) {
        guard let item = item else { return }
        Task {
            // PhotosPicker가 HEIC 등을 반환할 수 있어 JPEG로 재인코딩해 서버와 MIME을 맞춘다.
            if let transferableData = try? await item.loadTransferable(type: Data.self),
               let uiImage = UIImage(data: transferableData) {
                if let compressed = compressImageUnder1MB(uiImage) {
                    await viewModel.uploadProfileImage(imageData: compressed)
                } else {
                    await viewModel.setError("이미지를 압축할 수 없습니다.")
                }
            } else if let rawData = try? await item.loadTransferable(type: Data.self),
                      rawData.count <= 900_000 {
                // fallback: 원본이 이미 1MB 이하인 경우 그대로 업로드
                await viewModel.uploadProfileImage(imageData: rawData)
            } else {
                await viewModel.setError("이미지를 불러올 수 없습니다.")
            }
            selectedPhotoItem = nil
        }
    }

    /// 서버 제한(1MB) 이하로 JPEG 압축. 품질 조절 + 반복 리사이즈로 안전하게 0.9MB 이하로 맞춤.
    private func compressImageUnder1MB(_ image: UIImage) -> Data? {
        let maxBytes = 900_000 // 0.9MB로 여유 확보
        var quality: CGFloat = 0.75
        var currentImage = image

        for _ in 0..<5 { // 최대 5회 시도
            if let data = currentImage.jpegData(compressionQuality: quality), data.count <= maxBytes {
                return data
            }

            // 품질 조금 더 낮춤
            quality = max(0.5, quality - 0.1)

            // 크기 축소 비율 계산 (제곱근으로 넓이 기준 축소)
            if let data = currentImage.jpegData(compressionQuality: quality) {
                let ratio = sqrt(Double(maxBytes) / Double(max(data.count, 1)))
                let scale = max(0.4, min(0.9, CGFloat(ratio) * 0.95)) // 40% 이하로는 과도 축소 방지
                let newSize = CGSize(width: currentImage.size.width * scale, height: currentImage.size.height * scale)
                currentImage = resizeImage(currentImage, to: newSize) ?? currentImage
            }
        }
        // 마지막으로 한 번 더 시도
        return currentImage.jpegData(compressionQuality: 0.55)
    }

    private func resizeImage(_ image: UIImage, to size: CGSize) -> UIImage? {
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        image.draw(in: CGRect(origin: .zero, size: size))
        let resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resized
    }
}

// MARK: - Profile Header View

private struct ProfileHeaderView: View {
    let user: MyPageUserModel?
    let isUploadingImage: Bool
    let onImageTap: () -> Void
    let onCameraTap: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            // Profile Image
            ZStack(alignment: .bottomTrailing) {
                if let imageUrl = user?.profileImageUrl, !imageUrl.isEmpty {
                    CachedAsyncImage(url: imageUrl) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Circle()
                            .fill(AppColors.surfaceVariant)
                    }
                    .frame(width: 74, height: 74)
                    .clipShape(Circle())
                    .onTapGesture(perform: onImageTap)
                } else {
                    Circle()
                        .fill(AppColors.surfaceVariant)
                        .frame(width: 74, height: 74)
                        .overlay(
                            Image(systemName: "person.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 40)
                                .foregroundStyle(AppColors.onSurface.opacity(0.5))
                        )
                }

                // Loading overlay
                if isUploadingImage {
                    Circle()
                        .fill(AppColors.surface.opacity(0.7))
                        .frame(width: 74, height: 74)
                        .overlay(
                            ProgressView()
                                .scaleEffect(0.8)
                        )
                }

                // Camera button
                Button(action: onCameraTap) {
                    Circle()
                        .fill(AppColors.primary)
                        .frame(width: 26, height: 26)
                        .shadow(radius: 2)
                        .overlay(
                            Image(systemName: "camera.fill")
                                .font(.system(size: 12))
                                .foregroundStyle(AppColors.onPrimary)
                        )
                }
            }

            // User Info
            VStack(alignment: .leading, spacing: 4) {
                Text(user?.name ?? "사용자")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                Text(user?.displayMattermostId ?? "@-")
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                Text(user?.displayCampusInfo ?? "캠퍼스 정보 없음")
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface)
                    .padding(.top, 4)
            }

            Spacer()
        }
    }
}

// MARK: - Stats Row View

private struct StatsRowView: View {
    let counts: MyPageCountsModel?
    let onPostsTap: () -> Void
    let onCommentsTap: () -> Void
    let onScrapsTap: () -> Void

    var body: some View {
        HStack {
            StatItemView(label: "작성한 글", value: "\(counts?.postCount ?? 0)", onTap: onPostsTap)

            Divider()
                .frame(height: 28)
                .background(AppColors.onSurface.opacity(0.2))

            StatItemView(label: "댓글", value: "\(counts?.commentCount ?? 0)", onTap: onCommentsTap)

            Divider()
                .frame(height: 28)
                .background(AppColors.onSurface.opacity(0.2))

            StatItemView(label: "스크랩", value: "\(counts?.scrapCount ?? 0)", onTap: onScrapsTap)
        }
        .padding(14)
        .background(AppColors.surfaceVariant.opacity(0.4))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct StatItemView: View {
    let label: String
    let value: String
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Text(label)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                Text(value)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.primary)
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Portfolio Info Card

private struct PortfolioInfoCard: View {
    let summary: MyPagePortfolioSummaryModel?
    let isLoading: Bool
    let stackImageMap: [String: String]
    let onDetailTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                HStack(spacing: 12) {
                    Text("내 포트폴리오")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    if isLoading {
                        ProgressView()
                            .scaleEffect(0.8)
                    }
                }

                Spacer()

                Button(action: onDetailTap) {
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

            // Tech Stack
            SummarySectionView(icon: "chevron.left.forwardslash.chevron.right", label: "기술 스택") {
                if let techStack = summary?.techStack, !techStack.isEmpty {
                    let items = Array(techStack)
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        ForEach(items, id: \.key) { key, value in
                            HStack(spacing: 4) {
                                if let imgUrl = stackImageMap[key], !imgUrl.isEmpty {
                                    RemoteImageView(url: imgUrl, size: 16)
                                }
                                Text("\(key) (\(toLevelLabel(value)))")
                                    .font(.system(size: 12, weight: .medium))
                            }
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
            SummarySectionView(icon: "brain.head.profile", label: "SW 역량", value: summary?.ssafySwRating ?? "-")

            Spacer().frame(height: 16)

            // Solved.ac
            SummarySectionView(icon: "trophy", label: "Solved.ac") {
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
            SummarySectionView(icon: "link", label: "관련 링크 (블로그, 깃허브 등)") {
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
            SummarySectionView(icon: "briefcase", label: "프로젝트 경험") {
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

// MARK: - Summary Section View

private struct SummarySectionView<Content: View>: View {
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

extension SummarySectionView where Content == EmptyView {
    init(icon: String, label: String, value: String) {
        self.icon = icon
        self.label = label
        self.value = value
        self.content = nil
    }
}

// MARK: - Full Screen Image View

private struct FullScreenImageView: View {
    let imageUrl: String
    let onDismiss: () -> Void

    @State private var scale: CGFloat = 1.0
    @State private var offset: CGSize = .zero

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            CachedAsyncImage(url: imageUrl) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .scaleEffect(scale)
                    .offset(offset)
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

// MARK: - Preview

#Preview {
    MyPageView(
        viewModel: MyPageViewModel(myPageRepository: FakeMyPageRepository()),
        authRepository: FakeAuthRepository(),
        onLogout: {}
    )
}
