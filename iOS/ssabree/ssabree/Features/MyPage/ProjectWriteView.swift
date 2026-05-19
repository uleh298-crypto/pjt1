import SwiftUI
import PhotosUI

// MARK: - Project Write UI State

struct ProjectWriteUiState {
    var isLoading: Bool = false
    var isSaving: Bool = false
    var errorMessage: String? = nil
    var successMessage: String? = nil
    var project: ProjectModel? = nil
}

// MARK: - Project Write ViewModel

@Observable
final class ProjectWriteViewModel {
    private let projectRepository: ProjectRepository
    private let uploadRepository: UploadRepository?

    var uiState = ProjectWriteUiState()

    init(projectRepository: ProjectRepository, uploadRepository: UploadRepository? = nil) {
        self.projectRepository = projectRepository
        self.uploadRepository = uploadRepository
    }

    @MainActor
    func loadProject(portfolioId: Int, projectId: Int?) async {
        guard let projectId = projectId else { return }

        uiState.isLoading = true
        uiState.errorMessage = nil

        let result = await projectRepository.getProjectsByPortfolio(portfolioId: portfolioId)

        switch result {
        case .success(let projects):
            uiState.project = projects.first { $0.id == projectId }
            uiState.isLoading = false
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    @MainActor
    func saveProject(
        portfolioId: Int,
        projectId: Int?,
        title: String,
        introduction: String,
        description: String,
        techStacks: [String],
        urls: [String],
        imageUrls: [String]
    ) async {
        uiState.isSaving = true
        uiState.errorMessage = nil

        if let projectId = projectId {
            let info = ProjectUpdateInfo(
                title: title,
                introduction: introduction.isEmpty ? nil : introduction,
                description: description.isEmpty ? nil : description,
                techStacks: techStacks.filter { !$0.isEmpty },
                urls: urls.filter { !$0.isEmpty },
                imageUrls: imageUrls
            )
            let result = await projectRepository.updateProject(projectId: projectId, info: info)

            switch result {
            case .success:
                uiState.successMessage = "프로젝트가 수정되었습니다."
            case .failure(let error):
                uiState.errorMessage = error.localizedDescription
            }
        } else {
            let info = ProjectCreateInfo(
                portfolioId: portfolioId,
                title: title,
                introduction: introduction.isEmpty ? nil : introduction,
                description: description.isEmpty ? nil : description,
                techStacks: techStacks.filter { !$0.isEmpty },
                urls: urls.filter { !$0.isEmpty },
                imageUrls: imageUrls
            )
            let result = await projectRepository.createProject(info: info)

            switch result {
            case .success:
                uiState.successMessage = "프로젝트가 등록되었습니다."
            case .failure(let error):
                uiState.errorMessage = error.localizedDescription
            }
        }

        uiState.isSaving = false
    }

    @MainActor
    func uploadImage(_ imageData: Data) async -> String? {
        guard let uploadRepository = uploadRepository else { return nil }
        let result = await uploadRepository.uploadImage(image: imageData)
        switch result {
        case .success(let url):
            return url
        case .failure:
            return nil
        }
    }

    func clearSuccessMessage() {
        uiState.successMessage = nil
    }

    func clearErrorMessage() {
        uiState.errorMessage = nil
    }
}

// MARK: - Project Write View

struct ProjectWriteView: View {
    let portfolioId: Int
    let projectId: Int?
    @State var viewModel: ProjectWriteViewModel
    @State var stackViewModel: StackViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var introduction: String = ""
    @State private var descriptionText: String = ""
    @State private var selectedStacks: [SelectedStackItem] = []
    @State private var urls: [String] = []
    @State private var imageUrls: [String] = []

    @State private var initialTitle: String = ""
    @State private var initialIntroduction: String = ""
    @State private var initialDescription: String = ""
    @State private var initialStacks: [SelectedStackItem] = []
    @State private var initialUrls: [String] = []
    @State private var initialImageUrls: [String] = []

    @State private var showBackConfirmDialog = false
    @State private var showSuccessDialog = false
    @State private var showErrorDialog = false
    @State private var isUploadingImages = false
    @State private var selectedPhotoItems: [PhotosPickerItem] = []
    @State private var showStackEditSheet = false
    @State private var tempSelectedStacks: [SelectedStackItem] = []

    private var isSubmitEnabled: Bool {
        !title.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private var topBarTitle: String {
        projectId == nil ? "프로젝트 추가" : "프로젝트 수정"
    }

    private var hasChanges: Bool {
        title != initialTitle ||
        introduction != initialIntroduction ||
        descriptionText != initialDescription ||
        selectedStacks != initialStacks ||
        urls != initialUrls ||
        imageUrls != initialImageUrls
    }

    var body: some View {
        mainContent
            .alert("저장 확인", isPresented: $showBackConfirmDialog) {
                Button("아니오", role: .destructive) { dismiss() }
                Button("예") { Task { await saveProject() } }
            } message: {
                Text("저장되지 않은 내용이 있습니다. 저장하시겠습니까?")
            }
            .alert("알림", isPresented: $showSuccessDialog) {
                Button("확인") {
                    viewModel.clearSuccessMessage()
                    dismiss()
                }
            } message: {
                Text(viewModel.uiState.successMessage ?? "")
            }
            .alert("오류", isPresented: $showErrorDialog) {
                Button("확인") { viewModel.clearErrorMessage() }
            } message: {
                Text(viewModel.uiState.errorMessage ?? "")
            }
            .sheet(isPresented: $showStackEditSheet, onDismiss: applyStackChanges) {
                StackEditView(
                    allStacks: stackViewModel.stacks,
                    selectedStacks: $tempSelectedStacks,
                    showExpertLevel: false
                )
            }
    }

    private var mainContent: some View {
        VStack(spacing: 0) {
            header
            scrollContent
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task { await loadInitialData() }
        .onChange(of: viewModel.uiState.project) { _, newProject in
            if let project = newProject { syncFieldsFromProject(project) }
        }
        .onChange(of: viewModel.uiState.successMessage) { _, msg in
            showSuccessDialog = msg != nil
        }
        .onChange(of: viewModel.uiState.errorMessage) { _, msg in
            showErrorDialog = msg != nil
        }
        .onChange(of: selectedPhotoItems) { _, items in
            Task { await handlePhotoSelection(items) }
        }
        .overlay {
            if viewModel.uiState.isSaving {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
                    .tint(.white)
            }
        }
    }

    // MARK: - Data

    private func loadInitialData() async {
        await stackViewModel.loadStacks()
        if projectId != nil {
            await viewModel.loadProject(portfolioId: portfolioId, projectId: projectId)
        }
    }

    private func syncFieldsFromProject(_ project: ProjectModel) {
        title = project.title
        introduction = project.introduction ?? ""
        descriptionText = project.description ?? ""
        selectedStacks = project.techStacks.compactMap { name in
            if let stack = stackViewModel.stacks.first(where: { $0.name == name }) {
                return SelectedStackItem(id: stack.id, name: stack.name, imgUrl: stack.imgUrl)
            }
            return nil
        }
        urls = project.urls
        imageUrls = project.imageUrls

        initialTitle = title
        initialIntroduction = introduction
        initialDescription = descriptionText
        initialStacks = selectedStacks
        initialUrls = urls
        initialImageUrls = imageUrls
    }

    private func saveProject() async {
        await viewModel.saveProject(
            portfolioId: portfolioId,
            projectId: projectId,
            title: title.trimmingCharacters(in: .whitespaces),
            introduction: introduction.trimmingCharacters(in: .whitespaces),
            description: descriptionText.trimmingCharacters(in: .whitespaces),
            techStacks: selectedStacks.map { $0.name },
            urls: urls.map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty },
            imageUrls: imageUrls
        )
    }

    private func openStackEditSheet() {
        tempSelectedStacks = selectedStacks
        showStackEditSheet = true
    }

    private func applyStackChanges() {
        selectedStacks = tempSelectedStacks
    }

    // MARK: - Header

    private var header: some View {
        HStack {
            Button(action: {
                if hasChanges { showBackConfirmDialog = true } else { dismiss() }
            }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(topBarTitle)
                .font(.headline)
                .fontWeight(.bold)
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

    // MARK: - Scroll Content

    private var scrollContent: some View {
        ScrollView {
            VStack(spacing: 24) {
                titleSection
                introductionSection
                descriptionSection
                techStacksSection
                urlsSection
                imagesSection
                submitButton
                Spacer().frame(height: 16)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
        .background(AppColors.background)
    }

    // MARK: - Title

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("프로젝트 제목")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            TextField("프로젝트 제목을 입력하세요", text: $title)
                .padding()
                .background(AppColors.surfaceVariant.opacity(0.3))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Introduction

    private var introductionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("프로젝트 한 줄 소개")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            ZStack(alignment: .topLeading) {
                if introduction.isEmpty {
                    Text("간단 소개를 입력하세요")
                        .foregroundStyle(Color.gray)
                        .padding(.top, 16)
                        .padding(.leading, 16)
                }
                TextEditor(text: $introduction)
                    .frame(minHeight: 80)
                    .padding(12)
                    .scrollContentBackground(.hidden)
            }
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Description

    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("프로젝트 설명")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            ZStack(alignment: .topLeading) {
                if descriptionText.isEmpty {
                    Text("상세 설명을 입력하세요")
                        .foregroundStyle(Color.gray)
                        .padding(.top, 16)
                        .padding(.leading, 16)
                }
                TextEditor(text: $descriptionText)
                    .frame(minHeight: 150)
                    .padding(12)
                    .scrollContentBackground(.hidden)
            }
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Tech Stacks

    private var techStacksSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("기술 스택")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(AppColors.onSurface)

                Spacer()

                Button(action: openStackEditSheet) {
                    HStack(spacing: 4) {
                        Image(systemName: "pencil")
                            .font(.system(size: 12))
                        Text("편집")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .foregroundStyle(AppColors.primary)
                }
            }

            if selectedStacks.isEmpty {
                Text("기술 스택을 추가해주세요.")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(AppColors.surfaceVariant.opacity(0.3))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                // Selected stacks as chips
                FlowLayout(spacing: 8) {
                    ForEach(selectedStacks) { stack in
                        HStack(spacing: 6) {
                            if let imgUrl = stack.imgUrl, !imgUrl.isEmpty {
                                RemoteImageView(url: imgUrl, size: 18)
                            } else {
                                Image(systemName: "chevron.left.forwardslash.chevron.right")
                                    .font(.system(size: 10))
                                    .foregroundStyle(AppColors.primary)
                                    .frame(width: 18, height: 18)
                            }
                            Text(stack.name)
                                .font(.system(size: 13, weight: .medium))
                                .foregroundStyle(AppColors.onSurface)
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(AppColors.primaryContainer.opacity(0.3))
                        .clipShape(Capsule())
                    }
                }
            }
        }
    }

    // MARK: - URLs

    private var urlsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("관련 URL")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(AppColors.onSurface)
                Spacer()
                Button(action: { urls.append("") }) {
                    Image(systemName: "plus")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.primary)
                }
                if !urls.isEmpty {
                    Button(action: { urls.removeLast() }) {
                        Image(systemName: "minus")
                            .font(.system(size: 14))
                            .foregroundStyle(AppColors.primary)
                    }
                }
            }

            ForEach(urls.indices, id: \.self) { index in
                TextField("https://example.com", text: $urls[index])
                    .padding()
                    .background(AppColors.surfaceVariant.opacity(0.3))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    // MARK: - Images

    private var imagesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("이미지 (\(imageUrls.count)/10)")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(AppColors.onSurface)
                Spacer()
                if isUploadingImages {
                    ProgressView().scaleEffect(0.8)
                }
                PhotosPicker(
                    selection: $selectedPhotoItems,
                    maxSelectionCount: max(0, 10 - imageUrls.count),
                    matching: .images
                ) {
                    HStack(spacing: 4) {
                        Image(systemName: "photo")
                            .font(.system(size: 12))
                        Text("추가")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .foregroundStyle(imageUrls.count < 10 && !isUploadingImages ? AppColors.primary : AppColors.onSurface.opacity(0.4))
                }
                .disabled(imageUrls.count >= 10 || isUploadingImages)
            }

            if imageUrls.isEmpty {
                Text("이미지가 없습니다.")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(AppColors.surfaceVariant.opacity(0.3))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(imageUrls, id: \.self) { url in
                            ZStack(alignment: .topTrailing) {
                                CachedAsyncImage(url: url) { image in
                                    image.resizable().aspectRatio(contentMode: .fill)
                                } placeholder: {
                                    Rectangle().fill(AppColors.surfaceVariant)
                                }
                                .frame(width: 100, height: 100)
                                .clipShape(RoundedRectangle(cornerRadius: 12))

                                Button(action: { imageUrls.removeAll { $0 == url } }) {
                                    Image(systemName: "xmark")
                                        .font(.caption2)
                                        .fontWeight(.bold)
                                        .foregroundStyle(.white)
                                        .frame(width: 24, height: 24)
                                        .background(Color.black.opacity(0.5))
                                        .clipShape(Circle())
                                }
                                .padding(4)
                            }
                        }
                    }
                }
            }
        }
    }

    // MARK: - Submit Button

    private var submitButton: some View {
        HStack {
            Spacer()

            Button(action: { Task { await saveProject() } }) {
                Text("저장하기")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .frame(width: 140, height: 50)
                    .background(isSubmitEnabled ? AppColors.primary : AppColors.primary.opacity(0.4))
                    .clipShape(Capsule())
            }
            .disabled(!isSubmitEnabled || viewModel.uiState.isSaving)
        }
    }

    // MARK: - Image Handling

    private func handlePhotoSelection(_ items: [PhotosPickerItem]) async {
        guard !items.isEmpty else { return }

        isUploadingImages = true

        for item in items {
            guard let rawData = try? await item.loadTransferable(type: Data.self),
                  let uiImage = UIImage(data: rawData) else { continue }
            guard let compressed = compressImageForUpload(uiImage) else { continue }
            if let url = await viewModel.uploadImage(compressed) {
                if imageUrls.count < 10 {
                    imageUrls.append(url)
                }
            }
        }

        isUploadingImages = false
        selectedPhotoItems = []
    }

    /// 서버 제한(1MB) 이하로 JPEG 압축. 최대 1920px 리사이즈 후 품질 조절.
    private func compressImageForUpload(_ image: UIImage) -> Data? {
        let maxBytes = 1_000_000
        let maxDimension: CGFloat = 1920

        var currentImage = image
        if currentImage.size.width > maxDimension || currentImage.size.height > maxDimension {
            let scale = min(maxDimension / currentImage.size.width, maxDimension / currentImage.size.height)
            let newSize = CGSize(width: currentImage.size.width * scale, height: currentImage.size.height * scale)
            UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            currentImage.draw(in: CGRect(origin: .zero, size: newSize))
            if let resized = UIGraphicsGetImageFromCurrentImageContext() {
                currentImage = resized
            }
            UIGraphicsEndImageContext()
        }

        var quality: CGFloat = 0.9
        while quality > 0.1 {
            if let data = currentImage.jpegData(compressionQuality: quality), data.count <= maxBytes {
                return data
            }
            quality -= 0.1
        }
        return currentImage.jpegData(compressionQuality: 0.1)
    }
}

// MARK: - Flow Layout (for stack chips)

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
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
            maxX = max(maxX, x - spacing)
        }

        return (CGSize(width: maxX, height: y + rowHeight), positions)
    }
}

// MARK: - Stack ViewModel

@Observable
final class StackViewModel {
    private let stackRepository: StackRepository

    var stacks: [StackModel] = []

    init(stackRepository: StackRepository) {
        self.stackRepository = stackRepository
    }

    @MainActor
    func loadStacks() async {
        let result = await stackRepository.getStacks()
        switch result {
        case .success(let stacks):
            self.stacks = stacks
        case .failure:
            self.stacks = []
        }
    }
}
