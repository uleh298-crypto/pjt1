import SwiftUI

private enum GroupManageTab: String, CaseIterable {
    case study = "스터디"
    case project = "프로젝트"
}

private struct ManagedGroup: Identifiable {
    let id = UUID()
    let role: String
    let title: String
    let membersText: String
    let dDay: String
    let buttonTitle: String
    let avatars: [Color]
}

struct GroupManageView: View {
    var onMemberManageTap: () -> Void = {}
    var onGroupDetailTap: (Bool) -> Void = { _ in }
    @State private var selectedTab: GroupManageTab = .study

    private let groups: [ManagedGroup] = [
        ManagedGroup(
            role: "방장",
            title: "알고리즘 A to Z 스터디",
            membersText: "4/6명",
            dDay: "D-12",
            buttonTitle: "관리하기",
            avatars: [.pink, .purple, .orange, .gray]
        ),
        ManagedGroup(
            role: "팀원",
            title: "코틀린 마스터하기",
            membersText: "2/4명",
            dDay: "D-5",
            buttonTitle: "상세보기",
            avatars: [.mint, .yellow, .indigo]
        )
    ]

    var body: some View {
        VStack(spacing: 0) {
            topBar
            tabBar

            ScrollView {
                VStack(spacing: 12) {
                    ForEach(groups) { group in
                        GroupManageCard(
                            group: group,
                            onMemberManageTap: onMemberManageTap,
                            onDetailTap: { onGroupDetailTap(group.role == "방장") }
                        )
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
        }
        .background(AppColors.background.ignoresSafeArea())
    }

    private var topBar: some View {
        HStack {
            Button {
                // TODO: back action if needed
            } label: {
                Image(systemName: "chevron.left")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(AppColors.onSurface)
            }

            Text("나의 그룹")
                .font(.title3.weight(.semibold))
                .foregroundStyle(AppColors.onSurface)

            Spacer()

            Button {
                // TODO: search
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(AppColors.onSurface)
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 10)
        .padding(.bottom, 8)
    }

    private var tabBar: some View {
        HStack(spacing: 0) {
            ForEach(GroupManageTab.allCases, id: \.self) { tab in
                Button {
                    selectedTab = tab
                } label: {
                    Text(tab.rawValue)
                        .font(.headline)
                        .foregroundStyle(selectedTab == tab ? Color.black : Color.gray.opacity(0.6))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(
                            RoundedRectangle(cornerRadius: 22)
                                .fill(selectedTab == tab ? Color.white : Color.gray.opacity(0.2))
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 22)
                                .stroke(Color.gray.opacity(0.15), lineWidth: selectedTab == tab ? 1 : 0)
                        )
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 10)
    }
}

private struct GroupManageCard: View {
    let group: ManagedGroup
    var onMemberManageTap: () -> Void
    var onDetailTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(group.role)
                .font(.caption2.weight(.bold))
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.blue.opacity(0.15))
                .foregroundStyle(Color.blue)
                .clipShape(Capsule())

            Text(group.title)
                .font(.headline)
                .foregroundStyle(AppColors.onSurface)

            HStack(spacing: 10) {
                Label(group.membersText, systemImage: "person.2.fill")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Label(group.dDay, systemImage: "calendar")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            HStack {
                AvatarStack(colors: group.avatars)
                Spacer()
                Button(group.buttonTitle) {
                    if group.buttonTitle == "관리하기" {
                        onMemberManageTap()
                    } else {
                        onDetailTap()
                    }
                }
                    .font(.subheadline.weight(.semibold))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(group.buttonTitle == "관리하기" ? AppColors.primary : Color.gray.opacity(0.2))
                    .foregroundStyle(group.buttonTitle == "관리하기" ? Color.white : AppColors.onSurface)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .shadow(color: .black.opacity(0.05), radius: 6, x: 0, y: 4)
    }
}

private struct AvatarStack: View {
    let colors: [Color]

    var body: some View {
        HStack(spacing: -10) {
            ForEach(Array(colors.prefix(4).enumerated()), id: \.offset) { _, color in
                Circle()
                    .fill(color.opacity(0.6))
                    .frame(width: 26, height: 26)
            }
            Text("+1")
                .font(.caption2.weight(.semibold))
                .padding(6)
                .background(Color.gray.opacity(0.2))
                .clipShape(Circle())
        }
    }
}
