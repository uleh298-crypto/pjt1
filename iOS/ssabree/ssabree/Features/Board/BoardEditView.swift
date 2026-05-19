import SwiftUI

struct BoardEditView: View {
    let postId: Int
    @State var viewModel: BoardEditViewModel
    var onEditSuccess: (() -> Void)?
    @Environment(\.dismiss) private var dismiss

    // Dialog states
    @State private var showCancelDialog = false
    @State private var showSuccessDialog = false
    @State private var showErrorDialog = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            header

            if viewModel.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(AppColors.background)
            } else {
                ScrollView {
                    VStack(spacing: 24) {
                        // Title Input
                        titleSection

                        // Content Input
                        contentSection

                        // Submit Button
                        submitButton

                        Spacer().frame(height: 16)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
                .background(AppColors.background)
            }
        }
        .background(AppColors.background)
        .navigationBarHidden(true)
        .task {
            await viewModel.loadPost(postId: postId)
        }
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
        // Cancel Dialog
        .alert("게시글 수정 중단", isPresented: $showCancelDialog) {
            Button("계속 수정", role: .cancel) {}
            Button("중단", role: .destructive) { dismiss() }
        } message: {
            Text("수정을 중단하시겠습니까? 변경 사항이 저장되지 않습니다.")
        }
        // Success Dialog
        .alert("알림", isPresented: $showSuccessDialog) {
            Button("확인") {
                viewModel.clearSubmitSuccess()
                onEditSuccess?()
                dismiss()
            }
        } message: {
            Text("게시글을 수정했습니다.")
        }
        // Error Dialog
        .alert("오류", isPresented: $showErrorDialog) {
            Button("확인") {
                viewModel.clearSubmitError()
            }
        } message: {
            Text("게시글 수정에 실패했습니다.\n\(viewModel.uiState.submitError ?? "")")
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
                showCancelDialog = true
            }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("글 수정")
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

    // MARK: - Submit Button

    private var submitButton: some View {
        HStack {
            Spacer()

            Button(action: {
                Task { await viewModel.onSubmit() }
            }) {
                Text("수정 완료")
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
