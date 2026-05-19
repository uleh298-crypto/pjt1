import SwiftUI

// MARK: - Selected Stack Item

struct SelectedStackItem: Identifiable, Equatable {
    let id: Int  // stackId
    let name: String
    let imgUrl: String?
    var expertLevel: String  // "상", "중", "하"

    init(id: Int, name: String, imgUrl: String? = nil, expertLevel: String = "중") {
        self.id = id
        self.name = name
        self.imgUrl = imgUrl
        self.expertLevel = expertLevel
    }
}

// MARK: - Stack Edit View

struct StackEditView: View {
    let allStacks: [StackModel]
    @Binding var selectedStacks: [SelectedStackItem]
    var showExpertLevel: Bool = true
    @Environment(\.dismiss) private var dismiss

    @State private var searchQuery: String = ""

    private var filteredStacks: [StackModel] {
        let query = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
        if query.isEmpty {
            return allStacks
        }
        return allStacks.filter { $0.name.lowercased().contains(query) }
    }

    private var selectedIds: Set<Int> {
        Set(selectedStacks.map { $0.id })
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            headerView

            // Search Bar
            searchBar

            // Stack Grid
            ScrollView {
                stackGrid
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
    }

    // MARK: - Header View

    private var headerView: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "xmark")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("기술 스택 편집")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Button(action: { dismiss() }) {
                Text("완료")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.primary)
            }
        }
        .padding()
        .background(AppColors.surface)
    }

    // MARK: - Search Bar

    private var searchBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(AppColors.onSurface.opacity(0.5))

            TextField("기술 스택 검색", text: $searchQuery)
                .textFieldStyle(.plain)

            if !searchQuery.isEmpty {
                Button(action: { searchQuery = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundStyle(AppColors.onSurface.opacity(0.5))
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(AppColors.surfaceVariant.opacity(0.3))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    // MARK: - Stack Grid

    private var stackGrid: some View {
        let columns = [
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10)
        ]

        return LazyVGrid(columns: columns, spacing: 10) {
            ForEach(filteredStacks, id: \.id) { stack in
                StackCard(
                    stack: stack,
                    isSelected: selectedIds.contains(stack.id),
                    expertLevel: selectedStacks.first(where: { $0.id == stack.id })?.expertLevel,
                    showExpertLevel: showExpertLevel,
                    onTap: { toggleStack(stack) },
                    onLevelChange: { level in updateExpertLevel(stackId: stack.id, level: level) }
                )
            }
        }
    }

    // MARK: - Actions

    private func toggleStack(_ stack: StackModel) {
        if let index = selectedStacks.firstIndex(where: { $0.id == stack.id }) {
            selectedStacks.remove(at: index)
        } else {
            selectedStacks.append(SelectedStackItem(
                id: stack.id,
                name: stack.name,
                imgUrl: stack.imgUrl,
                expertLevel: "중"
            ))
        }
    }

    private func updateExpertLevel(stackId: Int, level: String) {
        if let index = selectedStacks.firstIndex(where: { $0.id == stackId }) {
            selectedStacks[index].expertLevel = level
        }
    }
}

// MARK: - Stack Card

private struct StackCard: View {
    let stack: StackModel
    let isSelected: Bool
    let expertLevel: String?
    var showExpertLevel: Bool = true
    let onTap: () -> Void
    let onLevelChange: (String) -> Void

    var body: some View {
        cardContent
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .padding(.horizontal, 8)
            .background(isSelected ? AppColors.primary : AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .shadow(color: .black.opacity(0.05), radius: 2, x: 0, y: 1)
            .onTapGesture { onTap() }
    }

    private var cardContent: some View {
        VStack(spacing: 6) {
            stackIcon
            stackLabel
            if showExpertLevel, isSelected, let level = expertLevel {
                levelPicker(level: level)
            }
        }
    }

    @ViewBuilder
    private var stackIcon: some View {
        if let imgUrl = stack.imgUrl, !imgUrl.isEmpty {
            RemoteImageView(url: imgUrl, size: 32)
        } else {
            Image(systemName: "chevron.left.forwardslash.chevron.right")
                .font(.system(size: 20))
                .foregroundStyle(isSelected ? Color.white.opacity(0.8) : AppColors.primary)
                .frame(width: 32, height: 32)
        }
    }

    private var stackLabel: some View {
        Text(stack.name)
            .font(.system(size: 12, weight: .medium))
            .foregroundStyle(isSelected ? Color.white : AppColors.onSurface)
            .lineLimit(1)
            .minimumScaleFactor(0.8)
    }

    private func levelPicker(level: String) -> some View {
        Menu {
            Button("상") { onLevelChange("상") }
            Button("중") { onLevelChange("중") }
            Button("하") { onLevelChange("하") }
        } label: {
            HStack(spacing: 2) {
                Text(level)
                    .font(.system(size: 10, weight: .bold))
                Image(systemName: "chevron.down")
                    .font(.system(size: 8))
            }
            .foregroundStyle(Color.white.opacity(0.9))
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(Color.white.opacity(0.2))
            .clipShape(Capsule())
        }
    }
}

// MARK: - Preview

#Preview {
    StackEditView(
        allStacks: [
            StackModel(id: 1, name: "Swift", imgUrl: nil),
            StackModel(id: 2, name: "Kotlin", imgUrl: nil),
            StackModel(id: 3, name: "Java", imgUrl: nil),
            StackModel(id: 4, name: "Python", imgUrl: nil),
            StackModel(id: 5, name: "JavaScript", imgUrl: nil),
            StackModel(id: 6, name: "TypeScript", imgUrl: nil),
            StackModel(id: 7, name: "React", imgUrl: nil),
            StackModel(id: 8, name: "Vue.js", imgUrl: nil),
            StackModel(id: 9, name: "Spring Boot", imgUrl: nil)
        ],
        selectedStacks: .constant([
            SelectedStackItem(id: 1, name: "Swift", expertLevel: "상"),
            SelectedStackItem(id: 2, name: "Kotlin", expertLevel: "중")
        ])
    )
}
