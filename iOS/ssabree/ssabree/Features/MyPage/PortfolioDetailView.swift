import SwiftUI
import PhotosUI

// MARK: - Portfolio Detail UI State

struct PortfolioDetailUiState {
    var isLoading: Bool = false
    var isSaving: Bool = false
    var errorMessage: String? = nil
    var portfolio: PortfolioModel? = nil
    var projects: [ProjectModel] = []
    var successMessage: String? = nil
}

// MARK: - Portfolio Detail ViewModel

@Observable
final class PortfolioDetailViewModel {
    private let portfolioRepository: PortfolioRepository
    private let projectRepository: ProjectRepository?
    private let stackRepository: StackRepository?
    private let uploadRepository: UploadRepository?

    private(set) var uiState = PortfolioDetailUiState()
    private(set) var stacks: [StackModel] = []

    init(
        portfolioRepository: PortfolioRepository,
        projectRepository: ProjectRepository? = nil,
        stackRepository: StackRepository? = nil,
        uploadRepository: UploadRepository? = nil
    ) {
        self.portfolioRepository = portfolioRepository
        self.projectRepository = projectRepository
        self.stackRepository = stackRepository
        self.uploadRepository = uploadRepository
    }

    @MainActor
    func loadMyPortfolio() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result = await portfolioRepository.getMyPortfolios()

        switch result {
        case .success(let portfolios):
            uiState.portfolio = portfolios.first
            uiState.isLoading = false
            if let portfolio = portfolios.first {
                await loadProjects(portfolioId: portfolio.id)
            }
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    @MainActor
    func loadProjects(portfolioId: Int) async {
        let result = await portfolioRepository.getProjectsByPortfolio(portfolioId: portfolioId)

        switch result {
        case .success(let projects):
            uiState.projects = projects
        case .failure(let error):
            print("[PortfolioDetailVM] loadProjects failed: \(error)")
        }
    }

    @MainActor
    func refreshProjects(portfolioId: Int) async {
        await loadProjects(portfolioId: portfolioId)
    }

    @MainActor
    func loadStacks() async {
        guard let stackRepository = stackRepository else { return }
        let result = await stackRepository.getStacks()
        switch result {
        case .success(let stacks):
            self.stacks = stacks
        case .failure(let error):
            print("[PortfolioDetailVM] loadStacks failed: \(error)")
        }
    }

    @MainActor
    func deleteProject(portfolioId: Int, projectId: Int) async {
        guard let projectRepository = projectRepository else { return }
        let result = await projectRepository.deleteProject(projectId: projectId)
        switch result {
        case .success:
            uiState.successMessage = "프로젝트가 삭제되었습니다."
            await loadProjects(portfolioId: portfolioId)
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
    }

    @MainActor
    func savePortfolio(
        title: String,
        description: String,
        introduction: String,
        bojHandle: String?,
        swTestRank: String?,
        isVisible: Bool,
        stacks: [PortfolioStackUpdateInfo],
        urls: [PortfolioUrlUpdateInfo],
        images: [PortfolioImageUpdateInfo]
    ) async -> Bool {
        let current = uiState.portfolio
        uiState.isSaving = true
        uiState.errorMessage = nil

        if current == nil {
            // Create new portfolio
            let createInfo = PortfolioCreateInfo(
                title: title.isEmpty ? "포트폴리오" : title,
                description: description.isEmpty ? "-" : description,
                introduction: introduction.isEmpty ? "-" : introduction,
                bojHandle: bojHandle,
                swTestRank: swTestRank,
                isVisible: isVisible,
                stacks: stacks,
                urls: urls,
                images: images
            )
            let result = await portfolioRepository.createPortfolio(info: createInfo)
            uiState.isSaving = false
            switch result {
            case .success:
                uiState.successMessage = "포트폴리오가 등록되었습니다."
                await loadMyPortfolio()
                return true
            case .failure(let error):
                uiState.errorMessage = error.localizedDescription
                return false
            }
        } else {
            // Update existing portfolio
            let updateInfo = PortfolioUpdateInfo(
                title: title.isEmpty ? current!.title : title,
                description: description.isEmpty ? current!.description : description,
                introduction: introduction.isEmpty ? current!.introduction : introduction,
                bojHandle: bojHandle ?? current!.bojHandle,
                swTestRank: swTestRank ?? current!.swTestRank,
                isVisible: isVisible,
                stacks: stacks,
                urls: urls,
                images: images
            )
            let result = await portfolioRepository.updatePortfolio(id: current!.id, info: updateInfo)
            uiState.isSaving = false
            switch result {
            case .success:
                uiState.successMessage = "포트폴리오가 수정되었습니다."
                await loadMyPortfolio()
                return true
            case .failure(let error):
                uiState.errorMessage = error.localizedDescription
                return false
            }
        }
    }

    @MainActor
    func verifySolvedacHandle(handle: String) async -> SolvedacVerifyInfo? {
        let result = await portfolioRepository.verifySolvedac(handle: handle)
        switch result {
        case .success(let info):
            return info
        case .failure:
            return nil
        }
    }

    @MainActor
    func uploadImage(_ imageData: Data) async -> String? {
        guard let uploadRepository = uploadRepository else { return nil }
        let result = await uploadRepository.uploadImage(image: imageData)
        switch result {
        case .success(let url):
            return url
        case .failure(let error):
            print("[PortfolioDetailVM] uploadImage failed: \(error)")
            return nil
        }
    }

    func clearSuccessMessage() {
        uiState.successMessage = nil
    }
}

// MARK: - Edit Mode Item Models

private struct StackEditItem: Equatable, Identifiable {
    let id = UUID()
    var stackId: Int?
    var stackName: String
    var expertLevelLabel: String

    init(stackId: Int? = nil, stackName: String = "", expertLevelLabel: String = "중") {
        self.stackId = stackId
        self.stackName = stackName
        self.expertLevelLabel = expertLevelLabel
    }
}

private struct UrlEditItem: Equatable, Identifiable {
    let id = UUID()
    var url: String

    init(url: String = "") {
        self.url = url
    }
}

private struct ImageEditItem: Equatable, Identifiable {
    let id = UUID()
    var imageUrl: String
    var orders: Int

    init(imageUrl: String = "", orders: Int = 0) {
        self.imageUrl = imageUrl
        self.orders = orders
    }
}

// MARK: - Portfolio Detail View

struct PortfolioDetailView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @State var viewModel: PortfolioDetailViewModel
    @State private var previewImageUrl: String? = nil

    // Edit Mode State
    @State private var isEditMode = false
    @State private var showBackConfirmDialog = false
    @State private var showSuccessDialog = false
    @State private var projectToDelete: ProjectModel? = nil

    // Edit Form Fields
    @State private var editTitle = ""
    @State private var editDescription = ""
    @State private var editIntroduction = ""
    @State private var editSolvedacHandle = ""
    @State private var editSolvedacRank = ""
    @State private var editSolvedacTierName = ""
    @State private var editSolvedacTierImageUrl = ""
    @State private var editSolvedacSolvedCount = ""
    @State private var editSwTestRank = ""
    @State private var editIsVisible = true
    @State private var editStacks: [StackEditItem] = []
    @State private var editUrls: [UrlEditItem] = []
    @State private var editImages: [ImageEditItem] = []

    // Backup state for cancel
    @State private var backupTitle = ""
    @State private var backupDescription = ""
    @State private var backupIntroduction = ""
    @State private var backupSolvedacHandle = ""
    @State private var backupSolvedacRank = ""
    @State private var backupSolvedacTierName = ""
    @State private var backupSolvedacTierImageUrl = ""
    @State private var backupSolvedacSolvedCount = ""
    @State private var backupSwTestRank = ""
    @State private var backupIsVisible = true
    @State private var backupStacks: [StackEditItem] = []
    @State private var backupUrls: [UrlEditItem] = []
    @State private var backupImages: [ImageEditItem] = []

    // Solvedac verification
    @State private var isSolvedacVerifying = false
    @State private var solvedacVerifyError: String? = nil

    // Image upload
    @State private var isUploadingImage = false
    @State private var selectedPhotoItem: PhotosPickerItem? = nil

    // Stack edit sheet
    @State private var showStackEditSheet = false
    @State private var tempSelectedStacks: [SelectedStackItem] = []

    init(viewModel: PortfolioDetailViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    private var hasEditChanges: Bool {
        editTitle != backupTitle ||
        editDescription != backupDescription ||
        editIntroduction != backupIntroduction ||
        editSolvedacHandle != backupSolvedacHandle ||
        editSolvedacRank != backupSolvedacRank ||
        editSolvedacTierName != backupSolvedacTierName ||
        editSolvedacTierImageUrl != backupSolvedacTierImageUrl ||
        editSolvedacSolvedCount != backupSolvedacSolvedCount ||
        editSwTestRank != backupSwTestRank ||
        editIsVisible != backupIsVisible ||
        editStacks != backupStacks ||
        editUrls != backupUrls ||
        editImages != backupImages
    }

    var body: some View {
        mainContent
            .navigationBarBackButtonHidden(true)
            .task {
                await viewModel.loadMyPortfolio()
                await viewModel.loadStacks()
            }
            .onChange(of: viewModel.uiState.portfolio) { _, portfolio in
                // 수정 모드가 아닐 때만 동기화 (수정 중 사용자 입력 보호)
                if !isEditMode {
                    syncEditFieldsFromPortfolio(portfolio)
                }
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                Task { await handlePhotoSelection(newItem) }
            }
            .onChange(of: viewModel.uiState.successMessage) { _, message in
                if message != nil { showSuccessDialog = true }
            }
            .fullScreenCover(item: $previewImageUrl) { imageUrl in
                ImagePreviewView(imageUrl: imageUrl, onDismiss: { previewImageUrl = nil })
            }
            .alert("저장 확인", isPresented: $showBackConfirmDialog, actions: backConfirmDialogActions, message: { Text("저장되지 않은 내용이 있습니다. 저장하시겠습니까?") })
            .alert("알림", isPresented: $showSuccessDialog, actions: { Button("확인") { viewModel.clearSuccessMessage() } }, message: { Text(viewModel.uiState.successMessage ?? "") })
            .alert("프로젝트 삭제", isPresented: showDeleteProjectBinding, actions: deleteProjectDialogActions, message: { Text("'\(projectToDelete?.title ?? "")' 프로젝트를 삭제하시겠습니까?") })
            .sheet(isPresented: $showStackEditSheet, onDismiss: applyStackChanges) {
                StackEditView(
                    allStacks: viewModel.stacks,
                    selectedStacks: $tempSelectedStacks
                )
            }
    }

    private var mainContent: some View {
        VStack(spacing: 0) {
            headerView
            ScrollView {
                VStack(spacing: 24) {
                    if viewModel.uiState.isLoading {
                        ProgressView().padding(.vertical, 40)
                    } else if let error = viewModel.uiState.errorMessage {
                        Text(error).foregroundStyle(AppColors.error).font(.system(size: 13))
                    } else if viewModel.uiState.portfolio == nil && !isEditMode {
                        Text("등록된 포트폴리오가 없습니다.").foregroundStyle(AppColors.onSurface.opacity(0.6)).font(.system(size: 13)).padding(.top, 40)
                    } else {
                        contentView
                    }
                    Spacer().frame(height: 16)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
    }

    private var showDeleteProjectBinding: Binding<Bool> {
        Binding(get: { projectToDelete != nil }, set: { if !$0 { projectToDelete = nil } })
    }

    @ViewBuilder
    private func backConfirmDialogActions() -> some View {
        Button("아니오", role: .destructive) {
            restoreFromBackup()
            isEditMode = false
        }
        Button("예") {
            Task { await savePortfolio() }
        }
    }

    @ViewBuilder
    private func deleteProjectDialogActions() -> some View {
        Button("취소", role: .cancel) { projectToDelete = nil }
        Button("삭제", role: .destructive) {
            if let project = projectToDelete, let portfolioId = viewModel.uiState.portfolio?.id {
                Task { await viewModel.deleteProject(portfolioId: portfolioId, projectId: project.id) }
            }
            projectToDelete = nil
        }
    }

    // MARK: - Header View

    private var headerView: some View {
        HStack {
            Button(action: handleBackAction) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(isEditMode ? "포트폴리오 수정" : "포트폴리오 상세")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Button(action: {
                if !isEditMode {
                    enterEditMode()
                } else {
                    Task {
                        await savePortfolio()
                    }
                }
            }) {
                Text(isEditMode ? "저장" : "수정")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.primary)
            }
            .disabled(viewModel.uiState.isSaving)
        }
        .padding()
        .background(AppColors.background)
    }

    // MARK: - Content View

    @ViewBuilder
    private var contentView: some View {
        let portfolio = viewModel.uiState.portfolio

        // Title
        PortfolioInfoCard {
            PortfolioSectionView(title: "제목", icon: "person.text.rectangle") {
                if isEditMode {
                    EditTextField(
                        value: $editTitle,
                        placeholder: "예) 백엔드 개발자 포트폴리오"
                    )
                } else {
                    Text((portfolio?.title ?? "").isEmpty ? "-" : portfolio!.title)
                        .font(.system(size: 15))
                        .foregroundStyle(AppColors.onSurface)
                }
            }
        }

        // Description
        PortfolioInfoCard {
            PortfolioSectionView(title: "한 줄 소개", icon: "text.alignleft") {
                if isEditMode {
                    EditTextField(
                        value: $editDescription,
                        placeholder: "한 줄 소개를 입력하세요"
                    )
                } else {
                    Text((portfolio?.description ?? "").isEmpty ? "-" : portfolio!.description)
                        .font(.system(size: 15))
                        .foregroundStyle(AppColors.onSurface)
                }
            }
        }

        // Introduction
        PortfolioInfoCard {
            PortfolioSectionView(title: "자기소개", icon: "person") {
                if isEditMode {
                    EditTextField(
                        value: $editIntroduction,
                        placeholder: "안녕하세요.",
                        axis: .vertical,
                        minLines: 3
                    )
                } else {
                    Text((portfolio?.introduction ?? "").isEmpty ? "-" : portfolio!.introduction)
                        .font(.system(size: 15))
                        .foregroundStyle(AppColors.onSurface)
                }
            }
        }

        // Tech Stacks
        PortfolioInfoCard {
            PortfolioSectionView(title: "기술 스택", icon: "chevron.left.forwardslash.chevron.right") {
                if isEditMode {
                    stacksEditView
                } else {
                    if portfolio?.stacks.isEmpty ?? true {
                        Text("-")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    } else {
                        StackChipsView(stacks: portfolio!.stacks)
                    }
                }
            }
        }

        // SW & Solved.ac
        PortfolioInfoCard {
            VStack(spacing: 16) {
                // SW Rating
                PortfolioSectionView(title: "SW 역량", icon: "brain.head.profile") {
                    if isEditMode {
                        SwTestRankPicker(selected: $editSwTestRank)
                    } else {
                        Text(portfolio?.swTestRank ?? "-")
                            .font(.system(size: 15))
                            .foregroundStyle(AppColors.onSurface)
                    }
                }

                // Solved.ac
                PortfolioSectionView(title: "Solved.ac 티어", icon: "trophy") {
                    if isEditMode {
                        solvedacEditView
                    } else {
                        SolvedAcInfoView(solvedAcInfo: portfolio?.solvedAcInfo)
                    }
                }
            }
        }

        // URLs
        PortfolioInfoCard {
            PortfolioSectionView(title: "관련 링크 (블로그, 깃허브 등)", icon: "link") {
                if isEditMode {
                    urlsEditView
                } else {
                    if portfolio?.urls.isEmpty ?? true {
                        Text("-")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    } else {
                        VStack(alignment: .leading, spacing: 6) {
                            ForEach(portfolio!.urls, id: \.id) { urlItem in
                                let displayUrl = urlItem.url.isEmpty ? "-" : urlItem.url
                                Text(displayUrl)
                                    .font(.system(size: 15))
                                    .foregroundStyle(AppColors.primary)
                                    .onTapGesture {
                                        openUrl(urlItem.url)
                                    }
                            }
                        }
                    }
                }
            }
        }

        // Projects
        PortfolioSectionView(
            title: "프로젝트 경험",
            icon: "briefcase",
            trailing: isEditMode && viewModel.uiState.portfolio != nil ? AnyView(
                NavigationLink(value: AppRoute.projectWrite(portfolioId: viewModel.uiState.portfolio!.id, projectId: nil)) {
                    Text("프로젝트 추가")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.primary)
                }
            ) : nil
        ) {
            if viewModel.uiState.projects.isEmpty {
                Text("-")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
            } else {
                VStack(spacing: 12) {
                    ForEach(viewModel.uiState.projects) { project in
                        ProjectExperienceCard(
                            project: project,
                            portfolioId: viewModel.uiState.portfolio?.id,
                            onOpenUrl: { openUrl($0) },
                            onImageClick: { previewImageUrl = $0 },
                            isEditMode: isEditMode,
                            onDeleteClick: isEditMode ? {
                                projectToDelete = project
                            } : nil
                        )
                    }
                }
            }
        }
    }

    // MARK: - Stacks Edit View

    private var stacksEditView: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Selected stacks display
            if editStacks.isEmpty {
                Text("선택된 기술 스택이 없습니다")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
            } else {
                FlowLayout(spacing: 8) {
                    ForEach(editStacks, id: \.id) { stack in
                        HStack(spacing: 4) {
                            Text(stack.stackName)
                                .font(.system(size: 12, weight: .medium))
                            Text("(\(stack.expertLevelLabel))")
                                .font(.system(size: 11))
                                .foregroundStyle(AppColors.onSurface.opacity(0.7))
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(AppColors.primaryContainer.opacity(0.3))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
            }

            // Edit button
            Button(action: openStackEditSheet) {
                HStack {
                    Image(systemName: "pencil")
                        .font(.system(size: 14))
                    Text("기술 스택 편집")
                        .font(.system(size: 14, weight: .medium))
                }
                .foregroundStyle(AppColors.primary)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(AppColors.primary.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
    }

    private func openStackEditSheet() {
        // Convert editStacks to tempSelectedStacks
        tempSelectedStacks = editStacks.compactMap { item -> SelectedStackItem? in
            guard let stackId = item.stackId else { return nil }
            return SelectedStackItem(
                id: stackId,
                name: item.stackName,
                imgUrl: viewModel.stacks.first(where: { $0.id == stackId })?.imgUrl,
                expertLevel: item.expertLevelLabel
            )
        }
        showStackEditSheet = true
    }

    private func applyStackChanges() {
        // Convert tempSelectedStacks back to editStacks
        editStacks = tempSelectedStacks.map { item in
            StackEditItem(
                stackId: item.id,
                stackName: item.name,
                expertLevelLabel: item.expertLevel
            )
        }
    }

    // MARK: - Solved.ac Edit View

    private var solvedacEditView: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Solved.ac 아이디")
                .font(.system(size: 14, weight: .bold))

            HStack(spacing: 8) {
                TextField("아이디 입력", text: $editSolvedacHandle)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: editSolvedacHandle) { _, _ in
                        solvedacVerifyError = nil
                    }

                Button("등록") {
                    Task {
                        await verifySolvedac()
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(editSolvedacHandle.trimmingCharacters(in: .whitespaces).isEmpty || isSolvedacVerifying)
            }

            if isSolvedacVerifying {
                ProgressView()
                    .scaleEffect(0.8)
            }

            if !editSolvedacTierName.isEmpty {
                Text("현재 티어: \(editSolvedacTierName)")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.primary)
            }

            if !editSolvedacHandle.isEmpty {
                let displayTier = editSolvedacTierName.isEmpty ? editSolvedacRank : editSolvedacTierName
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("아이디: \(editSolvedacHandle)")
                            .font(.system(size: 12))
                        Text("티어: \(displayTier.isEmpty ? "-" : displayTier)")
                            .font(.system(size: 12))
                        Text("푼 문제: \(editSolvedacSolvedCount.isEmpty ? "-" : editSolvedacSolvedCount)")
                            .font(.system(size: 12))
                    }
                    Spacer()
                    if !editSolvedacTierImageUrl.isEmpty {
                        AsyncImage(url: URL(string: editSolvedacTierImageUrl)) { image in
                            image.resizable().aspectRatio(contentMode: .fit)
                        } placeholder: {
                            EmptyView()
                        }
                        .frame(width: 28, height: 28)
                    }
                }
            }

            if let error = solvedacVerifyError {
                Text(error)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.error)
            }
        }
    }

    // MARK: - URLs Edit View

    private var urlsEditView: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("링크")
                    .font(.system(size: 14, weight: .bold))
                Spacer()
                Button(action: { editUrls.append(UrlEditItem()) }) {
                    Image(systemName: "plus")
                        .foregroundStyle(AppColors.primary)
                }
                Button(action: {
                    if !editUrls.isEmpty {
                        editUrls.removeLast()
                    }
                }) {
                    Image(systemName: "minus")
                        .foregroundStyle(AppColors.primary)
                }
            }

            ForEach(editUrls.indices, id: \.self) { index in
                TextField("https://example.com", text: $editUrls[index].url)
                    .textFieldStyle(.roundedBorder)
            }
        }
    }

    // MARK: - Actions

    private func handleBackAction() {
        if isEditMode {
            if hasEditChanges {
                showBackConfirmDialog = true
            } else {
                restoreFromBackup()
                isEditMode = false
            }
        } else {
            dismiss()
        }
    }

    private func enterEditMode() {
        // Backup current state
        backupTitle = editTitle
        backupDescription = editDescription
        backupIntroduction = editIntroduction
        backupSolvedacHandle = editSolvedacHandle
        backupSolvedacRank = editSolvedacRank
        backupSolvedacTierName = editSolvedacTierName
        backupSolvedacTierImageUrl = editSolvedacTierImageUrl
        backupSolvedacSolvedCount = editSolvedacSolvedCount
        backupSwTestRank = editSwTestRank
        backupIsVisible = editIsVisible
        backupStacks = editStacks
        backupUrls = editUrls
        backupImages = editImages
        solvedacVerifyError = nil
        isEditMode = true
    }

    private func restoreFromBackup() {
        editTitle = backupTitle
        editDescription = backupDescription
        editIntroduction = backupIntroduction
        editSolvedacHandle = backupSolvedacHandle
        editSolvedacRank = backupSolvedacRank
        editSolvedacTierName = backupSolvedacTierName
        editSolvedacTierImageUrl = backupSolvedacTierImageUrl
        editSolvedacSolvedCount = backupSolvedacSolvedCount
        editSwTestRank = backupSwTestRank
        editIsVisible = backupIsVisible
        editStacks = backupStacks
        editUrls = backupUrls
        editImages = backupImages
    }

    private func syncEditFieldsFromPortfolio(_ portfolio: PortfolioModel?) {
        guard let portfolio = portfolio else { return }
        editTitle = portfolio.title
        editDescription = portfolio.description
        editIntroduction = portfolio.introduction
        editSolvedacHandle = portfolio.bojHandle ?? ""
        editSolvedacRank = portfolio.solvedAcInfo?.tierName ?? ""
        editSolvedacTierName = portfolio.solvedAcInfo?.tierName ?? ""
        editSolvedacTierImageUrl = portfolio.solvedAcInfo?.tierImageUrl ?? ""
        editSolvedacSolvedCount = portfolio.solvedAcInfo?.solvedCount.map { "\($0)" } ?? ""
        editSwTestRank = portfolio.swTestRank ?? ""
        editIsVisible = portfolio.isVisible
        editStacks = portfolio.stacks.map {
            StackEditItem(stackId: $0.stackId, stackName: $0.stackName, expertLevelLabel: $0.expertLevelLabel)
        }
        editUrls = portfolio.urls.map { UrlEditItem(url: $0.url) }
        editImages = portfolio.images.map { ImageEditItem(imageUrl: $0.imageUrl, orders: $0.orders) }
    }

    private func savePortfolio() async {
        // 스택 목록이 비어 있으면 먼저 최신 목록을 불러와서 이름으로도 ID를 매핑할 수 있게 한다.
        if viewModel.stacks.isEmpty {
            await viewModel.loadStacks()
        }

        let stackInfos = editStacks.compactMap { item -> PortfolioStackUpdateInfo? in
            let trimmedName = item.stackName.trimmingCharacters(in: .whitespacesAndNewlines)
            // 사용자가 드롭다운을 선택하지 않아 stackId가 없는 경우, 이름과 일치하는 스택 ID를 찾아서 매핑
            let resolvedId = item.stackId ?? viewModel.stacks.first(where: { $0.name.caseInsensitiveCompare(trimmedName) == .orderedSame })?.id
            guard let stackId = resolvedId else { return nil }
            let level = toExpertLevelValue(item.expertLevelLabel)
            guard !level.isEmpty else { return nil }
            return PortfolioStackUpdateInfo(stackId: stackId, expertLevel: level)
        }

        let urlInfos = editUrls.compactMap { item -> PortfolioUrlUpdateInfo? in
            let url = item.url.trimmingCharacters(in: .whitespaces)
            guard !url.isEmpty else { return nil }
            return PortfolioUrlUpdateInfo(url: url)
        }

        let imageInfos = editImages.compactMap { item -> PortfolioImageUpdateInfo? in
            let url = item.imageUrl.trimmingCharacters(in: .whitespaces)
            guard !url.isEmpty else { return nil }
            return PortfolioImageUpdateInfo(imageUrl: url, orders: item.orders)
        }

        let success = await viewModel.savePortfolio(
            title: editTitle,
            description: editDescription,
            introduction: editIntroduction,
            bojHandle: editSolvedacHandle.isEmpty ? nil : editSolvedacHandle,
            swTestRank: editSwTestRank.isEmpty ? nil : editSwTestRank,
            isVisible: editIsVisible,
            stacks: stackInfos,
            urls: urlInfos,
            images: imageInfos
        )

        if success {
            isEditMode = false
            // 저장 성공 후 최신 데이터로 동기화
            syncEditFieldsFromPortfolio(viewModel.uiState.portfolio)
        }
    }

    private func verifySolvedac() async {
        let handle = editSolvedacHandle.trimmingCharacters(in: .whitespaces)
        guard !handle.isEmpty else { return }

        isSolvedacVerifying = true
        solvedacVerifyError = nil

        if let info = await viewModel.verifySolvedacHandle(handle: handle) {
            let tierLabel = solvedacTierLabel(info.tier)
            editSolvedacRank = tierLabel
            editSolvedacTierName = tierLabel
            editSolvedacTierImageUrl = solvedacTierImage(info.tier)
            editSolvedacSolvedCount = "\(info.solvedCount)"
        } else {
            solvedacVerifyError = "아이디를 확인할 수 없습니다."
        }

        isSolvedacVerifying = false
    }

    private func handlePhotoSelection(_ item: PhotosPickerItem?) async {
        guard let item = item else { return }
        guard let data = try? await item.loadTransferable(type: Data.self) else { return }

        isUploadingImage = true
        if let url = await viewModel.uploadImage(data) {
            let nextOrder = (editImages.map { $0.orders }.max() ?? 0) + 1
            editImages.append(ImageEditItem(imageUrl: url, orders: nextOrder))
        }
        isUploadingImage = false
        selectedPhotoItem = nil
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

    private func toExpertLevelValue(_ label: String) -> String {
        switch label {
        case "상": return "high"
        case "중": return "mid"
        case "하": return "low"
        default: return label
        }
    }

    private func solvedacTierLabel(_ tier: Int) -> String {
        switch tier {
        case ...0: return "Unrated"
        case 1...5: return "Bronze \(6 - tier)"
        case 6...10: return "Silver \(11 - tier)"
        case 11...15: return "Gold \(16 - tier)"
        case 16...20: return "Platinum \(21 - tier)"
        case 21...25: return "Diamond \(26 - tier)"
        case 26...30: return "Ruby \(31 - tier)"
        case 31: return "Master"
        default: return "Unknown"
        }
    }

    private func solvedacTierImage(_ tier: Int) -> String {
        if tier <= 0 { return "" }
        return "https://static.solved.ac/tier_small/\(tier).svg"
    }
}

// MARK: - Edit TextField

private struct EditTextField: View {
    @Binding var value: String
    let placeholder: String
    var axis: Axis = .horizontal
    var minLines: Int = 1

    var body: some View {
        if axis == .vertical {
            TextField(placeholder, text: $value, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(minLines...10)
        } else {
            TextField(placeholder, text: $value)
                .textFieldStyle(.roundedBorder)
        }
    }
}

// MARK: - Stack Search Field

private struct StackSearchField: View {
    let stacks: [StackModel]
    @Binding var selectedName: String
    @Binding var selectedId: Int?
    @State private var isExpanded = false
    @State private var isInitialized = false

    private var filteredStacks: [StackModel] {
        let query = selectedName.trimmingCharacters(in: .whitespaces)
        if query.isEmpty { return stacks }
        return stacks.filter { $0.name.lowercased().hasPrefix(query.lowercased()) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            TextField("기술 입력", text: $selectedName)
                .textFieldStyle(.roundedBorder)
                .onAppear {
                    isInitialized = true
                }
                .onChange(of: selectedName) { oldValue, newValue in
                    // 초기화 전이거나 동일한 값이면 무시
                    guard isInitialized, oldValue != newValue else { return }
                    // 사용자가 직접 타이핑한 경우에만 selectedId를 nil로 리셋
                    // 드롭다운에서 선택한 경우는 onTapGesture에서 처리됨
                    if selectedId != nil {
                        // 기존 선택된 스택의 이름과 다르면 리셋
                        let matchingStack = stacks.first { $0.id == selectedId }
                        if matchingStack?.name != newValue {
                            selectedId = nil
                        }
                    }
                    isExpanded = true
                }

            if isExpanded && !filteredStacks.isEmpty {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        ForEach(filteredStacks, id: \.id) { stack in
                            Text(stack.name)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(AppColors.surface)
                                .onTapGesture {
                                    selectedName = stack.name
                                    selectedId = stack.id
                                    isExpanded = false
                                }
                        }
                    }
                }
                .frame(maxHeight: 150)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
            }
        }
    }
}

// MARK: - Expert Level Picker

private struct ExpertLevelPicker: View {
    @Binding var selected: String
    private let options = ["상", "중", "하"]

    var body: some View {
        Menu {
            ForEach(options, id: \.self) { option in
                Button(option) {
                    selected = option
                }
            }
        } label: {
            HStack {
                Text("숙련도: \(selected)")
                    .font(.system(size: 14))
                Spacer()
                Image(systemName: "chevron.down")
                    .font(.system(size: 12))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .foregroundStyle(AppColors.onSurface)
    }
}

// MARK: - SW Test Rank Picker

private struct SwTestRankPicker: View {
    @Binding var selected: String
    private let options = ["IM", "A", "A+", "B"]

    var body: some View {
        Menu {
            ForEach(options, id: \.self) { option in
                Button(option) {
                    selected = option
                }
            }
        } label: {
            HStack {
                Text(selected.isEmpty ? "선택" : selected)
                    .font(.system(size: 14))
                Spacer()
                Image(systemName: "chevron.down")
                    .font(.system(size: 12))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .foregroundStyle(AppColors.onSurface)
    }
}

// MARK: - Portfolio Info Card

private struct PortfolioInfoCard<Content: View>: View {
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

// MARK: - Portfolio Section View

private struct PortfolioSectionView<Content: View>: View {
    let title: String
    let icon: String
    let trailing: AnyView?
    let content: Content

    init(title: String, icon: String, trailing: AnyView? = nil, @ViewBuilder content: () -> Content) {
        self.title = title
        self.icon = icon
        self.trailing = trailing
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

                Spacer()

                if let trailing = trailing {
                    trailing
                }
            }

            Spacer().frame(height: 12)

            HStack {
                Spacer().frame(width: 28)
                content
            }
        }
    }
}

// MARK: - Stack Chips View

private struct StackChipsView: View {
    let stacks: [PortfolioStackModel]

    var body: some View {
        let rows = stacks.chunked(into: 2)

        VStack(alignment: .leading, spacing: 8) {
            ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                HStack(spacing: 8) {
                    ForEach(row, id: \.id) { stack in
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
                    if row.count == 1 {
                        Spacer()
                    }
                }
            }
        }
    }
}

// MARK: - Solved.ac Info View

private struct SolvedAcInfoView: View {
    let solvedAcInfo: SolvedAcInfoModel?

    var body: some View {
        let displayTier = solvedAcInfo?.tierName ?? "-"
        let solvedCount = solvedAcInfo?.solvedCount

        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("티어: \(displayTier.isEmpty ? "-" : displayTier)")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface)

                Text("푼 문제: \(solvedCount.map { "\($0)" } ?? "-")")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface)
            }

            Spacer()

            if let tierImageUrl = solvedAcInfo?.tierImageUrl, !tierImageUrl.isEmpty {
                RemoteImageView(url: tierImageUrl, size: 28)
            }
        }
    }
}

// MARK: - Project Experience Card

private struct ProjectExperienceCard: View {
    let project: ProjectModel
    var portfolioId: Int? = nil
    let onOpenUrl: (String) -> Void
    let onImageClick: (String) -> Void
    var isEditMode: Bool = false
    var onDeleteClick: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // Title Header
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "briefcase")
                        .font(.system(size: 16))
                        .foregroundStyle(AppColors.primary)
                        .frame(width: 20)

                    Text("제목")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                }

                Spacer()

                if isEditMode {
                    HStack(spacing: 8) {
                        if let portfolioId = portfolioId {
                            NavigationLink(value: AppRoute.projectWrite(portfolioId: portfolioId, projectId: project.id)) {
                                Text("수정")
                                    .font(.system(size: 14))
                                    .foregroundStyle(AppColors.primary)
                            }
                        }
                        if let onDeleteClick = onDeleteClick {
                            Button("삭제", action: onDeleteClick)
                                .font(.system(size: 14))
                                .foregroundStyle(AppColors.error)
                        }
                    }
                }
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

// MARK: - Array Extension

private extension Array {
    func chunked(into size: Int) -> [[Element]] {
        stride(from: 0, to: count, by: size).map {
            Array(self[$0..<Swift.min($0 + size, count)])
        }
    }
}

// MARK: - String Identifiable Extension

extension String: @retroactive Identifiable {
    public var id: String { self }
}

// MARK: - Preview

#Preview {
    PortfolioDetailView(
        viewModel: PortfolioDetailViewModel(portfolioRepository: FakePortfolioRepository())
    )
}
