import SwiftUI
import Observation

// MARK: - UI State

struct MyApplicationsUiState {
    var isLoading: Bool = false
    var applications: [MyApplicationModel] = []
    var errorMessage: String? = nil
}

// MARK: - ViewModel

@Observable
@MainActor
final class MyApplicationsViewModel {
    private let groupRepository: GroupRepository
    private let groupKind: GroupKind

    var uiState = MyApplicationsUiState()

    init(groupRepository: GroupRepository, groupKind: GroupKind) {
        self.groupRepository = groupRepository
        self.groupKind = groupKind
    }

    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result: Result<[MyApplicationModel], Error>
        if groupKind == .study {
            result = await groupRepository.getMyStudyApplications()
        } else {
            result = await groupRepository.getMyTeamApplications()
        }

        switch result {
        case .success(let applications):
            let sorted = applications.sorted { a, b in
                if a.isPending != b.isPending {
                    return a.isPending
                }
                let aTime = parseCreatedAtMillis(a.createdAt)
                let bTime = parseCreatedAtMillis(b.createdAt)
                return aTime > bTime
            }
            uiState.applications = sorted
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
        uiState.isLoading = false
    }

    private func parseCreatedAtMillis(_ createdAt: String?) -> Int64 {
        guard let createdAt = createdAt else { return Int64.min }

        let inputFormatters: [DateFormatter] = {
            let formats = [
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
            ]
            return formats.map { format in
                let formatter = DateFormatter()
                formatter.dateFormat = format
                formatter.locale = Locale(identifier: "en_US_POSIX")
                formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
                return formatter
            }
        }()

        for formatter in inputFormatters {
            if let date = formatter.date(from: createdAt) {
                return Int64(date.timeIntervalSince1970 * 1000)
            }
        }
        return Int64.min
    }

    func cancelApplication(applicationId: Int) async {
        let result: Result<Void, Error>
        if groupKind == .study {
            result = await groupRepository.cancelStudyApplication(applicationId: applicationId)
        } else {
            result = await groupRepository.cancelTeamApplication(applicationId: applicationId)
        }

        switch result {
        case .success:
            await load()
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
    }

    func clearError() {
        uiState.errorMessage = nil
    }
}

// MARK: - View

struct MyApplicationsView: View {
    let groupKind: GroupKind
    @State var viewModel: MyApplicationsViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showCancelDialog = false
    @State private var pendingCancel: MyApplicationModel? = nil

    var body: some View {
        VStack(spacing: 0) {
            headerView
            contentView
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.load()
        }
        .refreshable {
            await viewModel.load()
        }
        .alert("지원 취소", isPresented: $showCancelDialog, presenting: pendingCancel) { application in
            Button("닫기", role: .cancel) { pendingCancel = nil }
            Button("취소", role: .destructive) {
                Task {
                    await viewModel.cancelApplication(applicationId: application.id)
                }
                pendingCancel = nil
            }
        } message: { application in
            Text("\(application.groupTitle) 지원을 취소하시겠습니까?")
        }
        .alert("오류", isPresented: Binding(
            get: { viewModel.uiState.errorMessage != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(viewModel.uiState.errorMessage ?? "")
        }
    }

    // MARK: - Header

    private var headerView: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }
            Spacer()
            Text(groupKind == .study ? "내 스터디 지원" : "내 프로젝트 지원")
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

    // MARK: - Content View

    @ViewBuilder
    private var contentView: some View {
        if viewModel.uiState.isLoading {
            VStack {
                Spacer()
                ProgressView()
                    .scaleEffect(1.2)
                Spacer()
            }
        } else if viewModel.uiState.applications.isEmpty {
            VStack {
                Spacer()
                Text("지원 내역이 없습니다.")
                    .foregroundStyle(AppColors.onSurfaceVariant)
                Spacer()
            }
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.uiState.applications) { application in
                        MyApplicationCard(
                            application: application,
                            onCancelClick: {
                                pendingCancel = application
                                showCancelDialog = true
                            }
                        )
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
        }
    }
}

// MARK: - Application Card

private struct MyApplicationCard: View {
    let application: MyApplicationModel
    let onCancelClick: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Icon + Title
            HStack(spacing: 6) {
                Image(systemName: "person.3.fill")
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurfaceVariant)
                    .frame(width: 18, height: 18)

                Text(application.groupTitle)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(AppColors.onSurface)
                    .lineLimit(1)
            }

            // Leader
            if let leaderName = application.leaderName, !leaderName.isEmpty {
                Text("팀장: \(leaderName)")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurfaceVariant)
            }

            // Date
            Text("지원일: \(application.formattedCreatedAt ?? "-")")
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onSurfaceVariant)

            // Status + Cancel
            HStack {
                Text(application.statusMessage)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(statusColor)

                Spacer()

                if application.isPending && !application.isGroupDeleted {
                    Button(action: onCancelClick) {
                        Text("지원 취소")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundStyle(AppColors.error)
                    }
                }
            }
        }
        .padding(16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.08), radius: 2, x: 0, y: 1)
    }

    private var statusColor: Color {
        switch application.status {
        case "APPROVED": return AppColors.primary
        case "REJECTED": return AppColors.error
        case "DELETED": return AppColors.onSurfaceVariant
        default: return AppColors.onSurfaceVariant
        }
    }
}
