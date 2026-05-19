import SwiftUI

struct MemberManageView: View {
    let groupId: Int
    let groupKind: GroupKind
    @State var viewModel: MemberManageViewModel
    @Environment(\.dismiss) private var dismiss

    // Dialog states
    @State private var showAcceptDialog = false
    @State private var showRejectDialog = false
    @State private var showKickDialog = false
    @State private var showFullGroupDialog = false
    @State private var selectedApplicant: GroupMemberModel? = nil
    @State private var selectedMember: GroupMemberModel? = nil

    // Toast
    @State private var showSuccessToast = false
    @State private var showErrorToast = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            headerView

            if viewModel.uiState.isLoading {
                Spacer()
                ProgressView()
                    .scaleEffect(1.2)
                Spacer()
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        // 멤버 요청 Section
                        memberRequestsSection

                        Spacer().frame(height: 32)

                        // 멤버 목록 Section
                        membersSection

                        Spacer().frame(height: 40)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadMembers(groupId: groupId, groupKind: groupKind)
        }
        .onChange(of: viewModel.uiState.successMessage) { _, message in
            if message != nil {
                showSuccessToast = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    showSuccessToast = false
                    viewModel.clearMessages()
                }
            }
        }
        .onChange(of: viewModel.uiState.error) { _, error in
            if error != nil {
                showErrorToast = true
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    showErrorToast = false
                    viewModel.clearMessages()
                }
            }
        }
        .overlay(alignment: .bottom) {
            if showSuccessToast, let message = viewModel.uiState.successMessage {
                ToastView(message: message, isError: false)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .padding(.bottom, 20)
            }
            if showErrorToast, let error = viewModel.uiState.error {
                ToastView(message: error, isError: true)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .padding(.bottom, 20)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showSuccessToast)
        .animation(.easeInOut(duration: 0.3), value: showErrorToast)
        // Dialogs
        .alert("수락 확인", isPresented: $showAcceptDialog, presenting: selectedApplicant) { applicant in
            Button("취소", role: .cancel) { selectedApplicant = nil }
            Button("수락") {
                Task {
                    await viewModel.acceptApplication(groupId: groupId, groupKind: groupKind, applicationId: applicant.id)
                }
                selectedApplicant = nil
            }
        } message: { applicant in
            Text("\(applicant.nickname ?? "신청자") 님의 지원을 수락하시겠습니까?")
        }
        .alert("거절 확인", isPresented: $showRejectDialog, presenting: selectedApplicant) { applicant in
            Button("취소", role: .cancel) { selectedApplicant = nil }
            Button("거절", role: .destructive) {
                Task {
                    await viewModel.rejectApplication(groupId: groupId, groupKind: groupKind, applicationId: applicant.id)
                }
                selectedApplicant = nil
            }
        } message: { applicant in
            Text("\(applicant.nickname ?? "신청자") 님의 지원을 거절하시겠습니까?")
        }
        .alert("멤버 추방", isPresented: $showKickDialog, presenting: selectedMember) { member in
            Button("취소", role: .cancel) { selectedMember = nil }
            Button("추방", role: .destructive) {
                Task {
                    await viewModel.removeMember(groupId: groupId, groupKind: groupKind, memberId: member.memberId)
                }
                selectedMember = nil
            }
        } message: { member in
            Text("\(member.nickname ?? "멤버") 님을 추방하시겠습니까?")
        }
        .alert("알림", isPresented: $showFullGroupDialog) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("그룹 인원이 꽉 찼습니다. 그룹 인원을 늘려주시길 바랍니다.")
        }
    }

    // MARK: - Header View

    private var headerView: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }
            Spacer()
            Text("멤버 관리")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)
            Spacer()
            // Balance
            Image(systemName: "chevron.left")
                .font(.title3)
                .foregroundStyle(.clear)
        }
        .padding()
        .background(AppColors.background)
    }

    // MARK: - Member Requests Section

    private var memberRequestsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("멤버 요청")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
                .padding(.top, 16)

            if viewModel.uiState.applicants.isEmpty {
                Text("아직 지원자가 없습니다.")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurfaceVariant)
                    .padding(.vertical, 4)
            } else {
                ForEach(viewModel.uiState.applicants) { applicant in
                    if let portfolioId = applicant.portfolioId, portfolioId > 0 {
                        NavigationLink(value: AppRoute.applicantPortfolio(portfolioId: portfolioId, applicationId: applicant.id, groupKind: groupKind)) {
                            MemberRequestCard(
                                applicant: applicant,
                                onAccept: { handleAccept(applicant) },
                                onReject: { handleReject(applicant) }
                            )
                        }
                        .buttonStyle(.plain)
                    } else {
                        MemberRequestCard(
                            applicant: applicant,
                            onAccept: { handleAccept(applicant) },
                            onReject: { handleReject(applicant) }
                        )
                    }
                }
            }
        }
    }

    // MARK: - Members Section

    private var membersSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("멤버 목록")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onSurface)

            VStack(spacing: 0) {
                if viewModel.uiState.members.isEmpty {
                    Text("멤버 정보가 없습니다.")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                        .padding(16)
                } else {
                    ForEach(Array(viewModel.uiState.members.enumerated()), id: \.element.id) { index, member in
                        let isLeader = member.memberId == viewModel.uiState.leaderId

                        if let portfolioId = member.portfolioId, portfolioId > 0 {
                            NavigationLink(value: AppRoute.applicantPortfolio(portfolioId: portfolioId)) {
                                MemberManageItem(
                                    member: member,
                                    isLeader: isLeader,
                                    onKick: { handleKick(member) }
                                )
                            }
                            .buttonStyle(.plain)
                        } else {
                            MemberManageItem(
                                member: member,
                                isLeader: isLeader,
                                onKick: { handleKick(member) }
                            )
                        }

                        if index != viewModel.uiState.members.count - 1 {
                            Divider()
                                .foregroundStyle(AppColors.outlineVariant.opacity(0.5))
                        }
                    }
                }
            }
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.03), radius: 2, x: 0, y: 1)
        }
    }

    // MARK: - Actions

    private func handleAccept(_ applicant: GroupMemberModel) {
        if viewModel.uiState.isFull {
            showFullGroupDialog = true
        } else {
            selectedApplicant = applicant
            showAcceptDialog = true
        }
    }

    private func handleReject(_ applicant: GroupMemberModel) {
        selectedApplicant = applicant
        showRejectDialog = true
    }

    private func handleKick(_ member: GroupMemberModel) {
        selectedMember = member
        showKickDialog = true
    }
}

// MARK: - Member Request Card (Android MemberRequestItem 동일 - 개별 둥근 카드)

private struct MemberRequestCard: View {
    let applicant: GroupMemberModel
    let onAccept: () -> Void
    let onReject: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Profile Image
            profileImage

            // Info
            VStack(alignment: .leading, spacing: 6) {
                Text(applicant.nickname ?? "-")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                // Position badge
                if let role = applicant.role, !role.isEmpty {
                    Text(role)
                        .font(.system(size: 11))
                        .foregroundStyle(AppColors.primary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(AppColors.primaryContainer.opacity(0.5))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }

            Spacer()

            // Action Buttons
            HStack(spacing: 8) {
                Button(action: onAccept) {
                    Text("수락")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 12)
                        .frame(height: 32)
                        .background(AppColors.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Button(action: onReject) {
                    Text("거절")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(AppColors.onSurface)
                        .padding(.horizontal, 12)
                        .frame(height: 32)
                        .background(AppColors.outlineVariant)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }
        }
        .padding(16)
        .background(AppColors.surfaceVariant.opacity(0.4))
        .clipShape(RoundedRectangle(cornerRadius: 30))
    }

    private var profileImage: some View {
        Group {
            if let url = applicant.profileImageUrl, !url.isEmpty {
                AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Circle().fill(AppColors.surfaceVariant)
                }
            } else {
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .overlay {
                        Image(systemName: "person.fill")
                            .font(.system(size: 20))
                            .foregroundStyle(AppColors.onSurface.opacity(0.4))
                    }
            }
        }
        .frame(width: 50, height: 50)
        .clipShape(Circle())
    }
}

// MARK: - Member Manage Item (Android MemberManageItem 동일 - 카드 안 리스트 아이템)

private struct MemberManageItem: View {
    let member: GroupMemberModel
    let isLeader: Bool
    let onKick: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Profile Image
            profileImage

            // Info
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(member.nickname ?? "멤버")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    if isLeader {
                        Text("팀장")
                            .font(.system(size: 11))
                            .foregroundStyle(AppColors.primary)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(AppColors.primaryContainer.opacity(0.5))
                            .clipShape(RoundedRectangle(cornerRadius: 4))
                    }
                }

                if let mmId = member.mattermostId, !mmId.isEmpty {
                    Text(mmId)
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }
            }

            Spacer()

            // Kick Button (non-leader only)
            if !isLeader {
                Button(action: onKick) {
                    Text("내보내기")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 8)
                        .frame(height: 28)
                        .background(AppColors.error)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }
        }
        .padding(16)
        .background(AppColors.surface)
    }

    private var profileImage: some View {
        Group {
            if let url = member.profileImageUrl, !url.isEmpty {
                AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Circle().fill(AppColors.surfaceVariant)
                }
            } else {
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .overlay {
                        Image(systemName: "person.fill")
                            .font(.system(size: 22))
                            .foregroundStyle(AppColors.onSurface.opacity(0.4))
                    }
            }
        }
        .frame(width: 52, height: 52)
        .clipShape(Circle())
    }
}

// MARK: - Toast View

private struct ToastView: View {
    let message: String
    let isError: Bool

    var body: some View {
        Text(message)
            .font(.system(size: 14, weight: .medium))
            .foregroundStyle(.white)
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(isError ? AppColors.error : AppColors.primary)
            .clipShape(Capsule())
            .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
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
