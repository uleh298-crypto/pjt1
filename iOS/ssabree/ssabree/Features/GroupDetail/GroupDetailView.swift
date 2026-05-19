import SwiftUI

struct GroupDetailView: View {
    let groupKind: GroupKind
    let groupId: Int
    @State var viewModel: GroupDetailViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showMenu = false
    @State private var showAlreadyMemberDialog = false
    @State private var showAlreadyAppliedDialog = false
    @State private var showDeleteDialog = false

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            topAppBar

            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let detail = viewModel.uiState.detail {
                // Content
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // Title
                        Text(detail.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundStyle(AppColors.onBackground)

                        // Leader Info
                        leaderInfoSection(detail: detail)

                        // Info Cards Row
                        HStack(spacing: 12) {
                            InfoSmallCard(
                                icon: "calendar",
                                label: "모집 종료일",
                                value: detail.endDateDisplay,
                                subValue: detail.dDay
                            )

                            InfoSmallCard(
                                icon: "person.2.fill",
                                label: "모집 인원",
                                value: "\(detail.capacity)명",
                                subValue: nil
                            )
                        }

                        // Description Card
                        descriptionCard(detail: detail)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }

                // Bottom Button
                bottomButton
            } else if let error = viewModel.uiState.error {
                Spacer()
                VStack(spacing: 16) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(AppColors.error)

                    Text("오류가 발생했습니다")
                        .font(.headline)
                        .foregroundStyle(AppColors.onSurface)

                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .multilineTextAlignment(.center)

                    Button("다시 시도") {
                        Task { await viewModel.loadDetail() }
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding()
                Spacer()
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadDetail()
        }
        .alert("알림", isPresented: $showAlreadyMemberDialog) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("이미 속한 그룹입니다.")
        }
        .alert("알림", isPresented: $showAlreadyAppliedDialog) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("이미 지원한 그룹입니다.")
        }
        .alert("삭제 확인", isPresented: $showDeleteDialog) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task {
                    await viewModel.deleteGroup()
                    dismiss()
                }
            }
        } message: {
            Text("해당 모집글을 삭제하시겠습니까?")
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

            Text("상세보기")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            if viewModel.uiState.isLeader {
                Menu {
                    NavigationLink(value: AppRoute.groupEdit(kind: groupKind, id: groupId)) {
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
            } else {
                // Placeholder for symmetry
                Image(systemName: "ellipsis")
                    .font(.title3)
                    .foregroundStyle(.clear)
            }
        }
        .padding()
        .background(Color.clear)
    }

    // MARK: - Leader Info Section

    @ViewBuilder
    private func leaderInfoSection(detail: GroupDetailModel) -> some View {
        HStack(spacing: 12) {
            // Profile Image
            if let profileUrl = detail.leaderProfileImageUrl, !profileUrl.isEmpty {
                AsyncImage(url: URL(string: normalizeImageUrl(profileUrl))) { image in
                    image
                        .resizable()
                        .scaledToFill()
                } placeholder: {
                    Circle()
                        .fill(AppColors.onSurface.opacity(0.1))
                        .overlay(
                            Image(systemName: "person.fill")
                                .foregroundStyle(AppColors.onSurface.opacity(0.5))
                        )
                }
                .frame(width: 64, height: 64)
                .clipShape(Circle())
                .background(Circle().fill(AppColors.onSurface.opacity(0.1)))
            } else {
                Circle()
                    .fill(AppColors.onSurface.opacity(0.1))
                    .frame(width: 64, height: 64)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.title)
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    )
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(detail.leaderName ?? "리더")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                if let mmId = detail.leaderMattermostId, !mmId.isEmpty {
                    Text(mmId)
                        .font(.system(size: 13))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }

                Text("팀장")
                    .font(.system(size: 15))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
            }

            Spacer()
        }
    }

    // MARK: - Description Card

    @ViewBuilder
    private func descriptionCard(detail: GroupDetailModel) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("상세 설명")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)

            Text(detail.description ?? "설명이 없습니다.")
                .font(.body)
                .lineSpacing(6)
                .foregroundStyle(AppColors.onSurface.opacity(0.8))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
    }

    // MARK: - Bottom Button

    private var bottomButton: some View {
        VStack(spacing: 0) {
            if viewModel.uiState.isMember {
                // 이미 가입된 멤버
                Button(action: {
                    showAlreadyMemberDialog = true
                }) {
                    Text("지원하기")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(AppColors.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 5))
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
            } else if viewModel.uiState.hasApplied {
                // 이미 지원한 상태
                Button(action: {
                    showAlreadyAppliedDialog = true
                }) {
                    Text("지원하기")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(AppColors.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 5))
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
            } else {
                // 지원 가능
                NavigationLink(value: AppRoute.groupApply(groupId: groupId, groupKind: groupKind)) {
                    Text("지원하기")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(AppColors.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 5))
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
            }
        }
        .background(AppColors.primary.opacity(0.1))
    }

    // MARK: - Helper Methods

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

// MARK: - Info Small Card

private struct InfoSmallCard: View {
    let icon: String
    let label: String
    let value: String
    let subValue: String?

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 28))
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
                .frame(width: 36, height: 36)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                Text(value)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                if let subValue = subValue {
                    Text(subValue)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(AppColors.primary)
                }
            }

            Spacer()
        }
        .padding(16)
        .frame(height: 100)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
    }
}
