import SwiftUI
import PhotosUI

struct BoardWriteView: View {
    @State var viewModel: BoardWriteViewModel
    var onWriteSuccess: (() -> Void)?
    @Environment(\.dismiss) private var dismiss

    // Dialog states
    @State private var showCancelDialog = false
    @State private var showSuccessDialog = false
    @State private var showErrorDialog = false

    // Image picker states
    @State private var showImageSourcePicker = false
    @State private var showPhotoPicker = false
    @State private var showCamera = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    var body: some View {
        @Bindable var viewModel = viewModel

        VStack(spacing: 0) {
            // Header
            header

            ScrollView {
                VStack(spacing: 24) {
                    // Board Selection
                    boardSelector

                    // Title Input
                    titleSection

                    // Content Input
                    contentSection

                    // Attached Images
                    if !viewModel.uiState.attachedImages.isEmpty {
                        attachedImagesSection
                    }

                    // Vote Section
                    if viewModel.uiState.isVoteEnabled {
                        voteSection
                    }

                    // Action Card
                    actionCard

                    // Submit Button
                    submitButton

                    Spacer().frame(height: 16)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .navigationBarHidden(true)
        .onChange(of: viewModel.uiState.isSubmitSuccess) { _, isSuccess in
            if isSuccess {
                showSuccessDialog = true
            }
        }
        .onChange(of: viewModel.uiState.submitError) { _, error in
            if error != nil {
                showErrorDialog = true
            }
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            Task {
                if let newItem,
                   let data = try? await newItem.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data) {
                    viewModel.onImageAttached(uiImage)
                }
                selectedPhotoItem = nil
            }
        }
        // Cancel Dialog
        .alert("게시글 작성 중단", isPresented: $showCancelDialog) {
            Button("계속 작성", role: .cancel) {}
            Button("중단", role: .destructive) { dismiss() }
        } message: {
            Text("작성 중인 내용이 있습니다. 게시글 작성을 중단하시겠습니까?")
        }
        // Success Dialog
        .alert("알림", isPresented: $showSuccessDialog) {
            Button("확인") {
                viewModel.clearSubmitSuccess()
                onWriteSuccess?()
                dismiss()
            }
        } message: {
            Text("게시글을 등록했습니다.")
        }
        // Error Dialog
        .alert("오류", isPresented: $showErrorDialog) {
            Button("확인") {
                viewModel.clearSubmitError()
            }
        } message: {
            Text("게시글 작성에 실패했습니다.\n\(viewModel.uiState.submitError ?? "")")
        }
        // Photo Picker
        .photosPicker(isPresented: $showPhotoPicker, selection: $selectedPhotoItem, matching: .images)
        // Camera
        .fullScreenCover(isPresented: $showCamera) {
            ImagePickerView(sourceType: .camera) { image in
                if let image {
                    viewModel.onImageAttached(image)
                }
            }
        }
        // Loading Overlay
        .overlay {
            if viewModel.uiState.isSubmitting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
                    .tint(.white)
            }
        }
    }

    // MARK: - Header

    private var header: some View {
        HStack {
            Button(action: {
                if viewModel.uiState.hasContent {
                    showCancelDialog = true
                } else {
                    dismiss()
                }
            }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("글 작성")
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

    // MARK: - Board Selector

    private var boardSelector: some View {
        Menu {
            ForEach(viewModel.uiState.boardOptions) { option in
                Button(option.name) {
                    viewModel.onBoardSelected(option.id)
                }
            }
        } label: {
            HStack {
                Text(viewModel.uiState.selectedBoardName)
                    .foregroundStyle(viewModel.uiState.selectedBoardId == nil ? Color.gray : AppColors.onSurface)
                Spacer()
                Image(systemName: "chevron.down")
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.7))
            }
            .padding()
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(AppColors.onSurface.opacity(0.2), lineWidth: 1)
            )
        }
        .disabled(viewModel.uiState.boardOptions.isEmpty)
    }

    // MARK: - Title Section

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("제목")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            TextField("제목을 입력하세요", text: Binding(
                get: { viewModel.uiState.title },
                set: { viewModel.onTitleChange($0) }
            ))
            .padding()
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Content Section

    private var contentSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("내용")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onSurface)

            ZStack(alignment: .topLeading) {
                if viewModel.uiState.content.isEmpty {
                    Text("내용을 입력하세요")
                        .foregroundStyle(Color.gray)
                        .padding(.top, 16)
                        .padding(.leading, 16)
                }

                TextEditor(text: Binding(
                    get: { viewModel.uiState.content },
                    set: { viewModel.onContentChange($0) }
                ))
                .frame(minHeight: 250)
                .padding(12)
                .scrollContentBackground(.hidden)
            }
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Attached Images Section

    private var attachedImagesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("첨부된 이미지 (\(viewModel.uiState.attachedImages.count)/5)")
                .font(.caption)
                .foregroundStyle(AppColors.onSurface.opacity(0.6))

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(viewModel.uiState.attachedImages.enumerated()), id: \.offset) { index, image in
                        ZStack(alignment: .topTrailing) {
                            Image(uiImage: image)
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 100, height: 100)
                                .clipShape(RoundedRectangle(cornerRadius: 12))

                            Button(action: {
                                viewModel.onRemoveImageAt(index)
                            }) {
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

    // MARK: - Vote Section

    private var voteSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("투표 설정")
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.primary)

            // Vote Title
            TextField("투표 제목을 입력하세요", text: Binding(
                get: { viewModel.uiState.voteTitle },
                set: { viewModel.onVoteTitleChange($0) }
            ))
            .padding()
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            // Vote Options
            ForEach(Array(viewModel.uiState.voteOptions.enumerated()), id: \.element.id) { index, option in
                HStack(spacing: 8) {
                    TextField("항목 \(index + 1)", text: Binding(
                        get: { option.text },
                        set: { viewModel.onVoteOptionChange(id: option.id, text: $0) }
                    ))
                    .padding()
                    .background(AppColors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    if viewModel.uiState.voteOptions.count > 2 {
                        Button(action: {
                            viewModel.onRemoveVoteOption(id: option.id)
                        }) {
                            Image(systemName: "minus.circle")
                                .foregroundStyle(.red)
                        }
                    }
                }
            }

            // Add Option Button
            if viewModel.uiState.voteOptions.count < 5 {
                Button(action: {
                    viewModel.onAddVoteOption()
                }) {
                    HStack {
                        Image(systemName: "plus")
                            .font(.caption)
                        Text("항목 추가")
                            .font(.subheadline)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                }
                .foregroundStyle(AppColors.primary)
            }
        }
        .padding(16)
        .background(AppColors.surfaceVariant.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Action Card

    private var actionCard: some View {
        let canAddImage = viewModel.canAddMoreImages

        return VStack(spacing: 0) {
            // Photo Attach Button
            Button(action: { showPhotoPicker = true }) {
                HStack {
                    Image(systemName: "photo")
                        .foregroundStyle(canAddImage ? Color.gray : Color.gray.opacity(0.4))
                    Spacer().frame(width: 12)
                    Text("사진 첨부")
                        .font(.subheadline)
                        .foregroundStyle(canAddImage ? AppColors.onSurface : AppColors.onSurface.opacity(0.4))
                    Spacer()
                    if !canAddImage {
                        Text("최대 5개")
                            .font(.caption)
                            .foregroundStyle(AppColors.onSurface.opacity(0.4))
                    }
                }
                .padding(16)
            }
            .disabled(!canAddImage)

            Divider()
                .padding(.horizontal, 16)

            // Camera Button
            Button(action: { showCamera = true }) {
                HStack {
                    Image(systemName: "camera")
                        .foregroundStyle(canAddImage ? Color.gray : Color.gray.opacity(0.4))
                    Spacer().frame(width: 12)
                    Text("카메라 촬영")
                        .font(.subheadline)
                        .foregroundStyle(canAddImage ? AppColors.onSurface : AppColors.onSurface.opacity(0.4))
                    Spacer()
                }
                .padding(16)
            }
            .disabled(!canAddImage)

            Divider()
                .padding(.horizontal, 16)

            // Vote Toggle Button
            Button(action: { viewModel.onToggleVote() }) {
                HStack {
                    Image(systemName: "chart.bar")
                        .foregroundStyle(viewModel.uiState.isVoteEnabled ? AppColors.primary : Color.gray)
                    Spacer().frame(width: 12)
                    Text(viewModel.uiState.isVoteEnabled ? "투표 삭제" : "투표 추가")
                        .font(.subheadline)
                        .foregroundStyle(viewModel.uiState.isVoteEnabled ? AppColors.primary : AppColors.onSurface)
                    Spacer()
                }
                .padding(16)
            }
        }
        .background(AppColors.surfaceVariant.opacity(0.3))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Submit Button

    private var submitButton: some View {
        HStack {
            Spacer()

            Button(action: {
                Task { await viewModel.onSubmit() }
            }) {
                Text("작성 완료")
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .frame(width: 140, height: 50)
                    .background(viewModel.uiState.isSubmitEnabled ? AppColors.primary : AppColors.primary.opacity(0.4))
                    .clipShape(Capsule())
            }
            .disabled(!viewModel.uiState.isSubmitEnabled)
        }
    }
}

// MARK: - Image Picker (for Camera)

struct ImagePickerView: UIViewControllerRepresentable {
    let sourceType: UIImagePickerController.SourceType
    let onImagePicked: (UIImage?) -> Void

    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onImagePicked: onImagePicked, dismiss: dismiss)
    }

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let onImagePicked: (UIImage?) -> Void
        let dismiss: DismissAction

        init(onImagePicked: @escaping (UIImage?) -> Void, dismiss: DismissAction) {
            self.onImagePicked = onImagePicked
            self.dismiss = dismiss
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            let image = info[.originalImage] as? UIImage
            onImagePicked(image)
            dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            onImagePicked(nil)
            dismiss()
        }
    }
}
