import SwiftUI

struct TaskDetailView: View {
    let groupId: Int
    let groupKind: GroupKind
    let taskId: Int
    @State var viewModel: TaskDetailViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showMenu = false
    @State private var showDeleteDialog = false

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }

                Spacer()

                Text("일정 상세")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                Spacer()

                Menu {
                    NavigationLink(value: AppRoute.editTask(groupId: groupId, groupKind: groupKind, taskId: taskId)) {
                        Label("수정", systemImage: "pencil")
                    }
                    Button("삭제", role: .destructive) {
                        showDeleteDialog = true
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            }
            .padding()
            .background(AppColors.background)

            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let task = viewModel.uiState.task {
                ScrollView {
                    VStack(spacing: 16) {
                        // Card
                        VStack(alignment: .leading, spacing: 12) {
                            // Author profile
                            authorSection

                            // Title field
                            readOnlyField(label: "제목", value: task.title)

                            // Content field
                            readOnlyField(label: "상세 내용", value: task.content)

                            // Dates row
                            HStack(spacing: 12) {
                                readOnlyField(label: "시작일", value: task.startDate)
                                readOnlyField(label: "종료일", value: task.endDate)
                            }

                            // Status field
                            readOnlyField(label: "상태", value: task.statusLabel)
                        }
                        .padding(16)
                        .background(AppColors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
                .background(AppColors.background)
            } else if let error = viewModel.uiState.errorMessage {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(AppColors.error)
                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(AppColors.onSurfaceVariant)
                    Button("다시 시도") {
                        Task { await viewModel.load() }
                    }
                    .buttonStyle(.borderedProminent)
                }
                Spacer()
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.load()
        }
        .overlay {
            if viewModel.uiState.isDeleting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
            }
        }
        .alert("삭제 확인", isPresented: $showDeleteDialog) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task { await viewModel.deleteTask() }
            }
        } message: {
            Text("일정을 삭제하시겠습니까?")
        }
        .alert("오류", isPresented: Binding(
            get: { viewModel.uiState.errorMessage != nil && !viewModel.uiState.isLoading },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(viewModel.uiState.errorMessage ?? "")
        }
        .onChange(of: viewModel.uiState.deleteSuccess) { _, success in
            if success { dismiss() }
        }
    }

    // MARK: - Author Section

    @ViewBuilder
    private var authorSection: some View {
        HStack(spacing: 12) {
            if let url = viewModel.uiState.authorProfileImageUrl, !url.isEmpty {
                AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Circle().fill(AppColors.surfaceVariant)
                }
                .frame(width: 40, height: 40)
                .clipShape(Circle())
            } else {
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .frame(width: 40, height: 40)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.system(size: 18))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                    )
            }

            Text("작성자: \(viewModel.uiState.authorName ?? "-")")
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onSurfaceVariant)

            Spacer()
        }
    }

    // MARK: - Read Only Field

    @ViewBuilder
    private func readOnlyField(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onSurfaceVariant)

            Text(value.isEmpty ? "-" : value)
                .font(.system(size: 15))
                .foregroundStyle(AppColors.onSurface)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
                .background(AppColors.surfaceVariant.opacity(0.2))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Helper

    private func normalizeImageUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return "" }
        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }
        let normalized = trimmed.replacingOccurrences(of: "\\", with: "/")
        if let uploadsIndex = normalized.range(of: "/uploads/") {
            let relative = String(normalized[uploadsIndex.lowerBound...])
            return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + relative
        }
        return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/uploads/" + normalized.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }
}
