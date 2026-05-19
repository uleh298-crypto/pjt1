import SwiftUI

struct GroupEditView: View {
    let groupKind: GroupKind
    let groupId: Int
    @State var viewModel: GroupEditViewModel
    @Environment(\.dismiss) private var dismiss

    // Form states
    @State private var titleText: String = ""
    @State private var descriptionText: String = ""
    @State private var selectedCategory: String = ""
    @State private var memberCount: Int = 3
    @State private var startDate: Date = Date()
    @State private var endDate: Date = Date()
    @State private var showDateSheet: Bool = false
    @State private var datePickerTab: Int = 0

    // Dialog
    @State private var showSuccessAlert: Bool = false

    private var recruitmentFields: [String] {
        groupKind == .study ? ["알고리즘", "CS", "자격증", "기타"] : ["싸피", "공모전", "자유"]
    }

    private var dateDisplayText: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yy/MM/dd"
        return "\(formatter.string(from: startDate)) ~ \(formatter.string(from: endDate))"
    }

    private var dDayText: String {
        let diff = Calendar.current.dateComponents([.day], from: Calendar.current.startOfDay(for: Date()), to: Calendar.current.startOfDay(for: endDate)).day ?? 0
        if diff >= 0 { return "D-\(diff)" }
        return "진행중"
    }

    private var isSubmitEnabled: Bool {
        !titleText.trimmingCharacters(in: .whitespaces).isEmpty &&
        !descriptionText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        startDate <= endDate
    }

    private var topBarTitle: String {
        "수정하기"
    }

    var body: some View {
        VStack(spacing: 0) {
            topBar

            if viewModel.uiState.isLoading && titleText.isEmpty {
                Spacer()
                ProgressView()
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 24) {
                        titleSection
                        categorySection
                        periodAndCapacitySection
                        descriptionSection
                        Spacer().frame(height: 24)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
                .background(AppColors.background)
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadDetail()
        }
        .onChange(of: viewModel.uiState.detail) { _, detail in
            if let detail = detail {
                populateFields(from: detail)
            }
        }
        .sheet(isPresented: $showDateSheet) {
            datePickerSheet
        }
        .alert("알림", isPresented: $showSuccessAlert) {
            Button("확인") {
                viewModel.resetResult()
                dismiss()
            }
        } message: {
            Text("수정이 완료되었습니다.")
        }
        .alert("오류", isPresented: Binding(get: { viewModel.uiState.errorMessage != nil }, set: { if !$0 { viewModel.uiState.errorMessage = nil } })) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(viewModel.uiState.errorMessage ?? "")
        }
        .onChange(of: viewModel.uiState.isSuccess) { _, isSuccess in
            if isSuccess { showSuccessAlert = true }
        }
        .onChange(of: startDate) { _, newValue in
            if endDate < newValue { endDate = newValue }
        }
        .overlay {
            if viewModel.uiState.isSubmitting {
                Color.black.opacity(0.25).ignoresSafeArea()
                ProgressView().tint(AppColors.primary)
            }
        }
    }

    private func populateFields(from detail: GroupDetailModel) {
        titleText = detail.title
        descriptionText = detail.description ?? ""
        memberCount = detail.capacity

        // Parse dates
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let startStr = detail.startDate, let start = formatter.date(from: startStr) {
            startDate = start
        }
        if let endStr = detail.endDate, let end = formatter.date(from: endStr) {
            endDate = end
        }

        // Map type to category
        if groupKind == .study {
            selectedCategory = GroupTypeMapper.studyTypeToLabel(detail.type)
        } else {
            selectedCategory = GroupTypeMapper.teamTypeToLabel(detail.type)
        }
    }

    // MARK: - Top Bar

    private var topBar: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(topBarTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Button(action: { Task { await submit() } }) {
                Text("저장")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(isSubmitEnabled ? AppColors.primary : AppColors.onSurface.opacity(0.4))
            }
            .disabled(!isSubmitEnabled || viewModel.uiState.isSubmitting)
        }
        .padding()
        .background(AppColors.background)
    }

    // MARK: - Sections

    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("제목")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
            TextField("제목을 입력하세요", text: $titleText)
                .padding()
                .background(AppColors.surfaceVariant.opacity(0.3))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var categorySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("모집 분야")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
            HStack(spacing: 8) {
                ForEach(recruitmentFields, id: \.self) { field in
                    let isSelected = selectedCategory == field
                    Text(field)
                        .font(.system(size: 13, weight: .bold))
                        .padding(.vertical, 10)
                        .padding(.horizontal, 16)
                        .background(isSelected ? AppColors.primary : AppColors.primary.opacity(0.4))
                        .foregroundStyle(isSelected ? AppColors.onPrimary : AppColors.onPrimary.opacity(0.9))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .onTapGesture { selectedCategory = field }
                }
            }
        }
    }

    private var periodAndCapacitySection: some View {
        HStack(alignment: .top, spacing: 16) {
            // 모집 기간 (더 넓은 공간 할당)
            VStack(alignment: .leading, spacing: 8) {
                Text("모집 기간")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
                Button(action: { showDateSheet = true }) {
                    HStack(spacing: 8) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(dateDisplayText)
                                .font(.system(size: 12))
                                .foregroundStyle(AppColors.onSurface)
                                .fixedSize(horizontal: true, vertical: false)
                            Text(dDayText)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundStyle(AppColors.primary)
                        }
                        Spacer(minLength: 4)
                        Image(systemName: "calendar")
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            .font(.system(size: 18))
                    }
                    .padding(12)
                    .frame(height: 56)
                    .frame(maxWidth: .infinity)
                    .background(AppColors.surfaceVariant.opacity(0.3))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .layoutPriority(1)

            // 모집 인원
            VStack(alignment: .leading, spacing: 8) {
                Text("모집 인원")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
                HStack {
                    Button(action: { if memberCount > 1 { memberCount -= 1 } }) {
                        Text("—")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity)

                    Text("\(memberCount)")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                        .frame(minWidth: 24)

                    Button(action: { memberCount += 1 }) {
                        Text("+")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity)
                }
                .frame(height: 56)
                .frame(maxWidth: .infinity)
                .background(AppColors.surfaceVariant.opacity(0.3))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .frame(width: 110)
        }
    }

    private var descriptionSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("상세 내용")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
            ZStack(alignment: .topLeading) {
                if descriptionText.isEmpty {
                    Text("프로젝트 소개, 진행 방식, 커리큘럼 등 상세 내용을 입력해 주세요")
                        .foregroundStyle(AppColors.onSurface.opacity(0.45))
                        .padding(.horizontal, 12)
                        .padding(.vertical, 12)
                }
                TextEditor(text: $descriptionText)
                    .frame(minHeight: 300)
                    .padding(8)
                    .scrollContentBackground(.hidden)
                    .background(AppColors.surfaceVariant.opacity(0.3))
            }
            .background(AppColors.surfaceVariant.opacity(0.3))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Date Picker Sheet

    private var datePickerSheet: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 12) {
                    Button {
                        datePickerTab = 0
                    } label: {
                        VStack(spacing: 4) {
                            Text("시작일")
                                .font(.system(size: 12))
                                .foregroundStyle(datePickerTab == 0 ? AppColors.primary : AppColors.onSurface.opacity(0.6))
                            Text(formatDate(startDate))
                                .font(.system(size: 16, weight: .bold))
                                .foregroundStyle(datePickerTab == 0 ? AppColors.primary : AppColors.onSurface)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(datePickerTab == 0 ? AppColors.primary.opacity(0.1) : Color.clear)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }

                    Image(systemName: "arrow.right")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface.opacity(0.4))

                    Button {
                        datePickerTab = 1
                    } label: {
                        VStack(spacing: 4) {
                            Text("종료일")
                                .font(.system(size: 12))
                                .foregroundStyle(datePickerTab == 1 ? AppColors.primary : AppColors.onSurface.opacity(0.6))
                            Text(formatDate(endDate))
                                .font(.system(size: 16, weight: .bold))
                                .foregroundStyle(datePickerTab == 1 ? AppColors.primary : AppColors.onSurface)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(datePickerTab == 1 ? AppColors.primary.opacity(0.1) : Color.clear)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)

                Divider()
                    .padding(.top, 12)

                if datePickerTab == 0 {
                    DatePicker(
                        "시작일",
                        selection: $startDate,
                        displayedComponents: .date
                    )
                    .datePickerStyle(.graphical)
                    .padding(.horizontal, 8)
                } else {
                    DatePicker(
                        "종료일",
                        selection: $endDate,
                        in: startDate...,
                        displayedComponents: .date
                    )
                    .datePickerStyle(.graphical)
                    .padding(.horizontal, 8)
                }

                Spacer()
            }
            .background(AppColors.background)
            .navigationTitle("모집 기간 선택")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("확인") { showDateSheet = false }
                        .fontWeight(.bold)
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button("취소") { showDateSheet = false }
                }
            }
        }
        .presentationDetents([.large])
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy.MM.dd"
        return formatter.string(from: date)
    }

    // MARK: - Submit

    private func submit() async {
        let typeValue: String = {
            if groupKind == .study {
                return GroupTypeMapper.studyLabelToApi(selectedCategory)
            } else {
                return GroupTypeMapper.teamLabelToApi(selectedCategory)
            }
        }()

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        let info = GroupUpdateInfo(
            title: titleText.trimmingCharacters(in: .whitespaces),
            type: typeValue,
            capacity: memberCount,
            startDate: formatter.string(from: startDate),
            endDate: formatter.string(from: endDate),
            description: descriptionText.trimmingCharacters(in: .whitespacesAndNewlines)
        )

        await viewModel.submit(info: info)
    }
}
