import SwiftUI

struct AddTaskView: View {
    let groupId: Int
    let groupKind: GroupKind
    @State var viewModel: AddTaskViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showStartDatePicker = false
    @State private var showEndDatePicker = false
    @State private var showStatusMenu = false
    @State private var showDateError = false
    @State private var dateErrorMessage = ""

    private let statusOptions = [
        ("TODO", "예정"),
        ("IN_PROGRESS", "진행중"),
        ("DONE", "완료")
    ]

    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.locale = Locale(identifier: "ko_KR")
        return f
    }()

    private var statusLabel: String {
        statusOptions.first(where: { $0.0 == viewModel.uiState.status })?.1 ?? "예정"
    }

    private var isFormValid: Bool {
        !viewModel.uiState.title.trimmingCharacters(in: .whitespaces).isEmpty &&
        !viewModel.uiState.content.trimmingCharacters(in: .whitespaces).isEmpty &&
        !viewModel.uiState.isPosting
    }

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
                Text("일정 추가")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
                Button(action: {
                    Task { await viewModel.createTask(groupId: groupId, groupKind: groupKind) }
                }) {
                    Text("등록")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(isFormValid ? AppColors.primary : AppColors.onSurfaceVariant)
                }
                .disabled(!isFormValid)
            }
            .padding()
            .background(AppColors.background)

            ScrollView {
                VStack(spacing: 16) {
                    // Section Header
                    HStack(spacing: 8) {
                        Image(systemName: "list.clipboard")
                            .font(.system(size: 18))
                            .foregroundStyle(AppColors.primary)
                        Text("일정 추가")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(AppColors.onSurface)
                        Spacer()
                    }

                    // Form Fields
                    VStack(spacing: 10) {
                        // Title
                        TextField("제목", text: $viewModel.uiState.title)
                            .font(.body)
                            .padding()
                            .background(AppColors.surfaceVariant.opacity(0.2))
                            .clipShape(RoundedRectangle(cornerRadius: 12))

                        // Content
                        ZStack(alignment: .topLeading) {
                            if viewModel.uiState.content.isEmpty {
                                Text("상세 내용")
                                    .foregroundStyle(AppColors.onSurfaceVariant)
                                    .padding(.top, 12)
                                    .padding(.leading, 16)
                            }
                            TextEditor(text: $viewModel.uiState.content)
                                .frame(minHeight: 120)
                                .padding(8)
                                .scrollContentBackground(.hidden)
                        }
                        .background(AppColors.surfaceVariant.opacity(0.2))
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                        // Date Pickers Row
                        HStack(spacing: 10) {
                            // Start Date
                            Button(action: { showStartDatePicker = true }) {
                                HStack {
                                    Text(viewModel.uiState.startDateString.isEmpty ? "시작일" : viewModel.uiState.startDateString)
                                        .font(.system(size: 14))
                                        .foregroundStyle(
                                            viewModel.uiState.startDateString.isEmpty
                                                ? AppColors.onSurfaceVariant
                                                : AppColors.onSurface
                                        )
                                    Spacer()
                                    Image(systemName: "calendar")
                                        .foregroundStyle(AppColors.onSurfaceVariant)
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 14)
                                .background(AppColors.surfaceVariant.opacity(0.2))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }

                            // End Date
                            Button(action: { showEndDatePicker = true }) {
                                HStack {
                                    Text(viewModel.uiState.endDateString.isEmpty ? "종료일" : viewModel.uiState.endDateString)
                                        .font(.system(size: 14))
                                        .foregroundStyle(
                                            viewModel.uiState.endDateString.isEmpty
                                                ? AppColors.onSurfaceVariant
                                                : AppColors.onSurface
                                        )
                                    Spacer()
                                    Image(systemName: "calendar")
                                        .foregroundStyle(AppColors.onSurfaceVariant)
                                }
                                .padding(.horizontal, 12)
                                .padding(.vertical, 14)
                                .background(AppColors.surfaceVariant.opacity(0.2))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }

                        // Status Dropdown
                        Menu {
                            ForEach(statusOptions, id: \.0) { value, label in
                                Button(label) {
                                    viewModel.uiState.status = value
                                }
                            }
                        } label: {
                            HStack {
                                Text(statusLabel)
                                    .font(.system(size: 14))
                                    .foregroundStyle(AppColors.onSurfaceVariant)
                                Spacer()
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 14))
                                    .foregroundStyle(AppColors.onSurfaceVariant)
                            }
                            .padding(16)
                            .background(AppColors.surfaceVariant.opacity(0.2))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
            }
            .background(AppColors.background)
        }
        .navigationBarBackButtonHidden(true)
        .overlay {
            if viewModel.uiState.isPosting {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView()
            }
        }
        .onChange(of: viewModel.uiState.isSuccess) { _, isSuccess in
            if isSuccess { dismiss() }
        }
        .alert("오류", isPresented: Binding(
            get: { viewModel.uiState.error != nil },
            set: { if !$0 { viewModel.uiState.error = nil } }
        )) {
            Button("확인", role: .cancel) {}
        } message: {
            if let error = viewModel.uiState.error {
                Text(error)
            }
        }
        .alert("날짜 오류", isPresented: $showDateError) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(dateErrorMessage)
        }
        .sheet(isPresented: $showStartDatePicker) {
            datePickerSheet(
                title: "시작일 선택",
                selection: Binding(
                    get: { viewModel.uiState.startDate },
                    set: { newDate in
                        let today = Calendar.current.startOfDay(for: Date())
                        if newDate < today {
                            dateErrorMessage = "시작일은 오늘 이전으로 설정할 수 없습니다."
                            showDateError = true
                            return
                        }
                        viewModel.uiState.startDate = newDate
                        viewModel.uiState.startDateString = dateFormatter.string(from: newDate)
                        // 종료일이 시작일 이전이면 초기화
                        if !viewModel.uiState.endDateString.isEmpty && viewModel.uiState.endDate < newDate {
                            viewModel.uiState.endDateString = ""
                        }
                        showStartDatePicker = false
                    }
                )
            )
        }
        .sheet(isPresented: $showEndDatePicker) {
            datePickerSheet(
                title: "종료일 선택",
                selection: Binding(
                    get: { viewModel.uiState.endDate },
                    set: { newDate in
                        if !viewModel.uiState.startDateString.isEmpty && newDate < viewModel.uiState.startDate {
                            dateErrorMessage = "종료일은 시작일 이후로 설정해야 합니다."
                            showDateError = true
                            return
                        }
                        viewModel.uiState.endDate = newDate
                        viewModel.uiState.endDateString = dateFormatter.string(from: newDate)
                        showEndDatePicker = false
                    }
                )
            )
        }
    }

    private func datePickerSheet(title: String, selection: Binding<Date>) -> some View {
        NavigationStack {
            DatePicker(title, selection: selection, displayedComponents: .date)
                .datePickerStyle(.graphical)
                .padding()
                .navigationTitle(title)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("취소") {
                            showStartDatePicker = false
                            showEndDatePicker = false
                        }
                    }
                }
        }
        .presentationDetents([.medium, .large])
    }
}
