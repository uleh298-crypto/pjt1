import SwiftUI

// MARK: - D-Day Detail Screen (Android: DdayDetailScreen)

struct DdayDetailView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: DdayDetailViewModel

    init(ddayRepository: DdayRepository) {
        _viewModel = State(initialValue: DdayDetailViewModel(ddayRepository: ddayRepository))
    }

    var body: some View {
        ZStack {
            AppColors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top Bar
                DdayTopBar(onBackClick: { dismiss() })

                ScrollView {
                    VStack(spacing: 16) {
                        // Calendar Card
                        DdayCalendarCard(
                            selectedDate: $viewModel.selectedDate,
                            localItems: viewModel.localItems,
                            remoteItems: viewModel.remoteItems,
                            onDateSelected: { viewModel.selectDate($0) }
                        )

                        // D-Day List
                        DdayListSection(
                            localItems: viewModel.localItems,
                            remoteItems: viewModel.remoteItems,
                            onItemTap: { viewModel.showEditDialog(for: $0) },
                            onAddTap: { viewModel.showAddDialog() }
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
            }

            // Loading
            if viewModel.isLoading {
                Color.black.opacity(0.1).ignoresSafeArea()
                ProgressView()
            }
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $viewModel.showEventDialog) {
            EventDialogSheet(
                date: viewModel.selectedDate,
                localItems: viewModel.localItems,
                remoteItems: viewModel.remoteItems,
                onAddDday: { viewModel.showAddDialog() },
                onDismiss: { viewModel.showEventDialog = false }
            )
            .presentationDetents([.medium])
        }
        .sheet(isPresented: $viewModel.showAddDdayDialog) {
            AddDdaySheet(
                initialDate: viewModel.selectedDate,
                onSave: { title, date, iconKey in
                    viewModel.addDday(title: title, date: date, iconKey: iconKey)
                },
                onDismiss: { viewModel.showAddDdayDialog = false }
            )
            .presentationDetents([.medium, .large])
        }
        .sheet(item: $viewModel.editingItem) { item in
            EditDdaySheet(
                item: item,
                onSave: { updatedItem in
                    viewModel.updateDday(updatedItem)
                },
                onDelete: {
                    viewModel.deleteDday(id: item.id)
                },
                onDismiss: { viewModel.editingItem = nil }
            )
            .presentationDetents([.medium, .large])
        }
    }
}

// MARK: - Top Bar

private struct DdayTopBar: View {
    let onBackClick: () -> Void

    var body: some View {
        HStack {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundColor(AppColors.onSurface)
            }
            .frame(width: 44, height: 44)

            Spacer()

            Text("D-Day")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(AppColors.onSurface)

            Spacer()

            Color.clear.frame(width: 44, height: 44)
        }
        .frame(height: 56)
        .padding(.horizontal, 8)
        .background(AppColors.surface)
    }
}

// MARK: - Calendar Card

private struct DdayCalendarCard: View {
    @Binding var selectedDate: Date
    let localItems: [LocalDdayItem]
    let remoteItems: [DdayItemModel]
    let onDateSelected: (Date) -> Void

    @State private var currentMonth: Date = Date()

    private let calendar = Calendar.current

    var body: some View {
        VStack(spacing: 12) {
            // Month Navigation
            HStack {
                Button(action: { changeMonth(by: -1) }) {
                    Image(systemName: "chevron.left")
                        .foregroundColor(AppColors.onSurface)
                }

                Spacer()

                Text(monthYearString)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(AppColors.onSurface)

                Spacer()

                Button(action: { changeMonth(by: 1) }) {
                    Image(systemName: "chevron.right")
                        .foregroundColor(AppColors.onSurface)
                }
            }
            .padding(.horizontal, 8)

            // Weekday Headers
            HStack(spacing: 0) {
                ForEach(["일", "월", "화", "수", "목", "금", "토"], id: \.self) { day in
                    Text(day)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(day == "일" ? .red : (day == "토" ? .blue : AppColors.onSurface.opacity(0.7)))
                        .frame(maxWidth: .infinity)
                }
            }

            // Calendar Grid
            MonthGridView(
                currentMonth: currentMonth,
                selectedDate: selectedDate,
                localItems: localItems,
                remoteItems: remoteItems,
                onDateSelected: onDateSelected
            )
        }
        .padding(16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }

    private var monthYearString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy년 M월"
        formatter.locale = Locale(identifier: "ko_KR")
        return formatter.string(from: currentMonth)
    }

    private func changeMonth(by offset: Int) {
        if let newMonth = calendar.date(byAdding: .month, value: offset, to: currentMonth) {
            currentMonth = newMonth
        }
    }
}

// MARK: - Month Grid View

private struct MonthGridView: View {
    let currentMonth: Date
    let selectedDate: Date
    let localItems: [LocalDdayItem]
    let remoteItems: [DdayItemModel]
    let onDateSelected: (Date) -> Void

    private let calendar = Calendar.current
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)

    var body: some View {
        let days = generateDays()

        LazyVGrid(columns: columns, spacing: 4) {
            ForEach(days, id: \.self) { date in
                if let date = date {
                    CalendarDayCell(
                        date: date,
                        isSelected: calendar.isDate(date, inSameDayAs: selectedDate),
                        isCurrentMonth: calendar.isDate(date, equalTo: currentMonth, toGranularity: .month),
                        isSalaryDay: checkSalaryDay(date),
                        hasDday: checkHasDday(date),
                        onTap: { onDateSelected(date) }
                    )
                } else {
                    Color.clear.frame(height: 40)
                }
            }
        }
    }

    private func generateDays() -> [Date?] {
        var days: [Date?] = []

        guard let monthInterval = calendar.dateInterval(of: .month, for: currentMonth),
              let firstWeek = calendar.dateInterval(of: .weekOfMonth, for: monthInterval.start) else {
            return days
        }

        var date = firstWeek.start
        let endDate = monthInterval.end

        // 6주 표시
        for _ in 0..<42 {
            if date < endDate || days.count % 7 != 0 {
                days.append(date)
            } else {
                break
            }
            date = calendar.date(byAdding: .day, value: 1, to: date) ?? date
        }

        // 빈 셀 채우기
        while days.count < 42 && days.count % 7 != 0 {
            days.append(nil)
        }

        return days
    }

    private func checkSalaryDay(_ date: Date) -> Bool {
        let year = calendar.component(.year, from: date)
        let month = calendar.component(.month, from: date)
        let day = calendar.component(.day, from: date)
        return DdayLocalStore.isSalaryDay(year: year, month: month, day: day)
    }

    private func checkHasDday(_ date: Date) -> Bool {
        let formatter = DateFormatter()

        // 로컬 D-Day 체크 (yyyy.MM.dd)
        formatter.dateFormat = "yyyy.MM.dd"
        let localDateStr = formatter.string(from: date)
        if localItems.contains(where: { $0.date == localDateStr }) {
            return true
        }

        // 원격 D-Day 체크 (yyyy-MM-dd)
        formatter.dateFormat = "yyyy-MM-dd"
        let remoteDateStr = formatter.string(from: date)
        if remoteItems.contains(where: { $0.targetDate == remoteDateStr }) {
            return true
        }

        return false
    }
}

// MARK: - Calendar Day Cell

private struct CalendarDayCell: View {
    let date: Date
    let isSelected: Bool
    let isCurrentMonth: Bool
    let isSalaryDay: Bool
    let hasDday: Bool
    let onTap: () -> Void

    private let calendar = Calendar.current

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 2) {
                Text("\(calendar.component(.day, from: date))")
                    .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                    .foregroundColor(textColor)

                // 표시자들
                HStack(spacing: 2) {
                    if isSalaryDay {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 4, height: 4)
                    }
                    if hasDday {
                        Circle()
                            .fill(Color.blue)
                            .frame(width: 4, height: 4)
                    }
                }
                .frame(height: 6)
            }
            .frame(width: 40, height: 40)
            .background(isSelected ? AppColors.primary.opacity(0.2) : Color.clear)
            .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .opacity(isCurrentMonth ? 1 : 0.3)
    }

    private var textColor: Color {
        let weekday = calendar.component(.weekday, from: date)
        if weekday == 1 { return .red }
        if weekday == 7 { return .blue }
        return AppColors.onSurface
    }
}

// MARK: - D-Day List Section

private struct DdayListSection: View {
    let localItems: [LocalDdayItem]
    let remoteItems: [DdayItemModel]
    let onItemTap: (LocalDdayItem) -> Void
    let onAddTap: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("D-Day 목록")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(AppColors.onSurface)

                Spacer()

                Button(action: onAddTap) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 24))
                        .foregroundColor(AppColors.primary)
                }
            }

            // Legend
            HStack(spacing: 16) {
                LegendItem(color: .green, label: "월급날")
                LegendItem(color: .blue, label: "D-Day")
            }
            .font(.system(size: 12))

            // Remote D-Days
            if !remoteItems.isEmpty {
                Text("공식 일정")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(AppColors.onSurface.opacity(0.7))
                    .padding(.top, 8)

                ForEach(remoteItems) { item in
                    DdayListItemView(
                        title: item.title,
                        date: item.displayDate,
                        dDay: item.dDayLabel,
                        iconKey: item.iconKey,
                        isEditable: false,
                        onTap: {}
                    )
                }
            }

            // Local D-Days
            if !localItems.isEmpty {
                Text("내 D-Day")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(AppColors.onSurface.opacity(0.7))
                    .padding(.top, 8)

                ForEach(localItems) { item in
                    DdayListItemView(
                        title: item.title,
                        date: item.date,
                        dDay: DdayLocalStore.formatDdayLabel(days: DdayLocalStore.calculateDays(from: item.date)),
                        iconKey: item.iconKey,
                        isEditable: true,
                        showOnHome: item.showOnHome,
                        onTap: { onItemTap(item) }
                    )
                }
            }

            if localItems.isEmpty && remoteItems.isEmpty {
                Text("등록된 D-Day가 없습니다.")
                    .font(.system(size: 14))
                    .foregroundColor(AppColors.onSurface.opacity(0.6))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
            }
        }
        .padding(16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }
}

private struct LegendItem: View {
    let color: Color
    let label: String

    var body: some View {
        HStack(spacing: 4) {
            Circle()
                .fill(color)
                .frame(width: 8, height: 8)
            Text(label)
                .foregroundColor(AppColors.onSurface.opacity(0.7))
        }
    }
}

// MARK: - D-Day List Item View

private struct DdayListItemView: View {
    let title: String
    let date: String
    let dDay: String
    let iconKey: String?
    let isEditable: Bool
    var showOnHome: Bool = true
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Icon
                Image(systemName: iconForKey(iconKey))
                    .font(.system(size: 20))
                    .foregroundColor(AppColors.primary)
                    .frame(width: 40, height: 40)
                    .background(AppColors.primary.opacity(0.1))
                    .clipShape(Circle())

                // Info
                VStack(alignment: .leading, spacing: 2) {
                    HStack {
                        Text(title)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(AppColors.onSurface)

                        if !showOnHome {
                            Text("숨김")
                                .font(.system(size: 10))
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.gray)
                                .clipShape(Capsule())
                        }
                    }

                    Text(date)
                        .font(.system(size: 12))
                        .foregroundColor(AppColors.onSurface.opacity(0.6))
                }

                Spacer()

                // D-Day Label
                Text(dDay)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(AppColors.primary)

                if isEditable {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12))
                        .foregroundColor(AppColors.onSurface.opacity(0.3))
                }
            }
            .padding(12)
            .background(AppColors.background)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .disabled(!isEditable)
    }

    private func iconForKey(_ key: String?) -> String {
        guard let key = key else { return "calendar" }
        // 간단한 매핑 (Android의 아이콘 키에 맞춤)
        switch key.lowercased() {
        case "code", "terminal", "laptop": return "laptopcomputer"
        case "school", "book", "study": return "book.fill"
        case "star", "achievement": return "star.fill"
        case "cake", "celebration": return "birthday.cake.fill"
        case "work", "business": return "briefcase.fill"
        case "payments", "salary": return "banknote.fill"
        default: return "calendar"
        }
    }
}

// MARK: - Event Dialog Sheet

private struct EventDialogSheet: View {
    let date: Date
    let localItems: [LocalDdayItem]
    let remoteItems: [DdayItemModel]
    let onAddDday: () -> Void
    let onDismiss: () -> Void

    private let calendar = Calendar.current

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // 날짜 헤더
                Text(dateString)
                    .font(.system(size: 18, weight: .semibold))

                // 월급날 표시
                if isSalaryDay {
                    HStack(spacing: 8) {
                        Image(systemName: "banknote.fill")
                            .foregroundColor(.green)
                        Text("월급날")
                            .font(.system(size: 14, weight: .medium))
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.green.opacity(0.1))
                    .clipShape(Capsule())
                }

                // 이벤트 목록
                List {
                    ForEach(eventsForDate) { event in
                        HStack {
                            Image(systemName: "calendar")
                                .foregroundColor(AppColors.primary)
                            Text(event.title)
                            Spacer()
                            Text(event.dDay)
                                .foregroundColor(AppColors.primary)
                        }
                    }
                }
                .listStyle(.plain)

                // 추가 버튼
                Button(action: onAddDday) {
                    HStack {
                        Image(systemName: "plus")
                        Text("새 D-Day 추가")
                    }
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(AppColors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.horizontal)
            }
            .padding(.vertical)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("닫기", action: onDismiss)
                }
            }
        }
    }

    private var dateString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy년 M월 d일 (E)"
        formatter.locale = Locale(identifier: "ko_KR")
        return formatter.string(from: date)
    }

    private var isSalaryDay: Bool {
        let year = calendar.component(.year, from: date)
        let month = calendar.component(.month, from: date)
        let day = calendar.component(.day, from: date)
        return DdayLocalStore.isSalaryDay(year: year, month: month, day: day)
    }

    private var eventsForDate: [EventItem] {
        var events: [EventItem] = []

        let formatter = DateFormatter()

        // 로컬 D-Day
        formatter.dateFormat = "yyyy.MM.dd"
        let localDateStr = formatter.string(from: date)
        for item in localItems where item.date == localDateStr {
            let days = DdayLocalStore.calculateDays(from: item.date)
            events.append(EventItem(id: item.id, title: item.title, dDay: DdayLocalStore.formatDdayLabel(days: days)))
        }

        // 원격 D-Day
        formatter.dateFormat = "yyyy-MM-dd"
        let remoteDateStr = formatter.string(from: date)
        for item in remoteItems where item.targetDate == remoteDateStr {
            events.append(EventItem(id: item.id, title: item.title, dDay: item.dDayLabel))
        }

        return events
    }
}

private struct EventItem: Identifiable {
    let id: Int
    let title: String
    let dDay: String
}

// MARK: - Add D-Day Sheet

private struct AddDdaySheet: View {
    let initialDate: Date
    let onSave: (String, String, String?) -> Void
    let onDismiss: () -> Void

    @State private var title = ""
    @State private var selectedDate: Date
    @State private var selectedIconKey: String? = nil

    init(initialDate: Date, onSave: @escaping (String, String, String?) -> Void, onDismiss: @escaping () -> Void) {
        self.initialDate = initialDate
        self.onSave = onSave
        self.onDismiss = onDismiss
        _selectedDate = State(initialValue: initialDate)
    }

    var body: some View {
        NavigationView {
            Form {
                Section("D-Day 정보") {
                    TextField("제목", text: $title)

                    DatePicker("날짜", selection: $selectedDate, displayedComponents: .date)
                        .environment(\.locale, Locale(identifier: "ko_KR"))
                }

                Section("아이콘 선택") {
                    IconPickerGrid(selectedIconKey: $selectedIconKey)
                }
            }
            .navigationTitle("새 D-Day")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소", action: onDismiss)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("저장") {
                        let formatter = DateFormatter()
                        formatter.dateFormat = "yyyy.MM.dd"
                        let dateString = formatter.string(from: selectedDate)
                        onSave(title, dateString, selectedIconKey)
                        onDismiss()
                    }
                    .disabled(title.isEmpty)
                }
            }
        }
    }
}

// MARK: - Edit D-Day Sheet

private struct EditDdaySheet: View {
    let item: LocalDdayItem
    let onSave: (LocalDdayItem) -> Void
    let onDelete: () -> Void
    let onDismiss: () -> Void

    @State private var title: String
    @State private var selectedDate: Date
    @State private var showOnHome: Bool
    @State private var selectedIconKey: String?
    @State private var showDeleteConfirm = false

    init(item: LocalDdayItem, onSave: @escaping (LocalDdayItem) -> Void, onDelete: @escaping () -> Void, onDismiss: @escaping () -> Void) {
        self.item = item
        self.onSave = onSave
        self.onDelete = onDelete
        self.onDismiss = onDismiss

        _title = State(initialValue: item.title)
        _showOnHome = State(initialValue: item.showOnHome)
        _selectedIconKey = State(initialValue: item.iconKey)

        // 날짜 파싱
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy.MM.dd"
        _selectedDate = State(initialValue: formatter.date(from: item.date) ?? Date())
    }

    var body: some View {
        NavigationView {
            Form {
                Section("D-Day 정보") {
                    TextField("제목", text: $title)

                    DatePicker("날짜", selection: $selectedDate, displayedComponents: .date)
                        .environment(\.locale, Locale(identifier: "ko_KR"))

                    Toggle("홈 화면에 표시", isOn: $showOnHome)
                }

                Section("아이콘 선택") {
                    IconPickerGrid(selectedIconKey: $selectedIconKey)
                }

                Section {
                    Button(role: .destructive) {
                        showDeleteConfirm = true
                    } label: {
                        HStack {
                            Spacer()
                            Text("삭제")
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("D-Day 수정")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("취소", action: onDismiss)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("저장") {
                        let formatter = DateFormatter()
                        formatter.dateFormat = "yyyy.MM.dd"
                        let dateString = formatter.string(from: selectedDate)

                        let updatedItem = LocalDdayItem(
                            id: item.id,
                            title: title,
                            date: dateString,
                            showOnHome: showOnHome,
                            iconKey: selectedIconKey
                        )
                        onSave(updatedItem)
                        onDismiss()
                    }
                    .disabled(title.isEmpty)
                }
            }
            .alert("D-Day 삭제", isPresented: $showDeleteConfirm) {
                Button("취소", role: .cancel) {}
                Button("삭제", role: .destructive) {
                    onDelete()
                    onDismiss()
                }
            } message: {
                Text("'\(item.title)'를 삭제하시겠습니까?")
            }
        }
    }
}

// MARK: - Icon Picker Grid

private struct IconPickerGrid: View {
    @Binding var selectedIconKey: String?

    private let icons: [(key: String, symbol: String)] = [
        ("calendar", "calendar"),
        ("code", "laptopcomputer"),
        ("book", "book.fill"),
        ("star", "star.fill"),
        ("cake", "birthday.cake.fill"),
        ("work", "briefcase.fill"),
        ("heart", "heart.fill"),
        ("flag", "flag.fill"),
        ("bell", "bell.fill"),
        ("gift", "gift.fill"),
        ("plane", "airplane"),
        ("car", "car.fill")
    ]

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 12), count: 6)

    var body: some View {
        LazyVGrid(columns: columns, spacing: 12) {
            ForEach(icons, id: \.key) { icon in
                Button {
                    selectedIconKey = icon.key
                } label: {
                    Image(systemName: icon.symbol)
                        .font(.system(size: 20))
                        .foregroundColor(selectedIconKey == icon.key ? .white : AppColors.onSurface)
                        .frame(width: 44, height: 44)
                        .background(selectedIconKey == icon.key ? AppColors.primary : AppColors.background)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 8)
    }
}

// MARK: - ViewModel

@Observable
final class DdayDetailViewModel {
    private let ddayRepository: DdayRepository
    private let localStore = DdayLocalStore.shared

    var isLoading = false
    var remoteItems: [DdayItemModel] = []
    var localItems: [LocalDdayItem] = []
    var selectedDate = Date()
    var showEventDialog = false
    var showAddDdayDialog = false
    var editingItem: LocalDdayItem?

    init(ddayRepository: DdayRepository) {
        self.ddayRepository = ddayRepository
        Task { await load() }
    }

    @MainActor
    func load() async {
        isLoading = true

        // 원격 D-Day 로드
        if case .success(let items) = await ddayRepository.fetchDdays() {
            remoteItems = items
        }

        // 로컬 D-Day 로드
        localItems = localStore.load()

        isLoading = false
    }

    func selectDate(_ date: Date) {
        selectedDate = date
        showEventDialog = true
    }

    func showAddDialog() {
        showEventDialog = false
        showAddDdayDialog = true
    }

    func showEditDialog(for item: LocalDdayItem) {
        editingItem = item
    }

    func addDday(title: String, date: String, iconKey: String?) {
        let newItem = LocalDdayItem(
            id: localStore.getNextId(),
            title: title,
            date: date,
            showOnHome: true,
            iconKey: iconKey
        )
        localStore.add(newItem)
        localItems = localStore.load()
    }

    func updateDday(_ item: LocalDdayItem) {
        localStore.update(item)
        localItems = localStore.load()
    }

    func deleteDday(id: Int) {
        localStore.delete(id: id)
        localItems = localStore.load()
    }
}
