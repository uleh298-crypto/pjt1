import SwiftUI
import Observation

// MARK: - UI State

struct InquiryUiState {
    var isLoading: Bool = false
    var isSubmitting: Bool = false
    var errorMessage: String? = nil
    var inquiries: [InquiryModel] = []
}

// MARK: - ViewModel

@Observable
@MainActor
final class InquiryViewModel {
    private let inquiryRepository: InquiryRepository

    var uiState = InquiryUiState()

    init(inquiryRepository: InquiryRepository) {
        self.inquiryRepository = inquiryRepository
    }

    func loadInquiries() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result = await inquiryRepository.getInquiries()
        switch result {
        case .success(let inquiries):
            uiState.inquiries = inquiries
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func submitInquiry(content: String) async -> Bool {
        guard !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return false }

        uiState.isSubmitting = true
        uiState.errorMessage = nil

        let result = await inquiryRepository.createInquiry(content: content)
        uiState.isSubmitting = false

        switch result {
        case .success:
            await loadInquiries()
            return true
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            return false
        }
    }
}

// MARK: - Screen State

private enum InquiryScreenState {
    case main
    case write
    case detail(InquiryItem)
}

// MARK: - UI Model

private struct InquiryItem: Identifiable {
    let id: Int
    let title: String
    let content: String
    let date: String
    let answer: String?
}

// MARK: - View

struct InquiryView: View {
    @State var viewModel: InquiryViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var screenState: InquiryScreenState = .main
    @State private var inquiryTitle: String = ""
    @State private var inquiryContent: String = ""

    var body: some View {
        VStack(spacing: 0) {
            headerView

            Group {
                switch screenState {
                case .main:
                    mainContent
                case .write:
                    writeContent
                case .detail(let item):
                    detailContent(item)
                }
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadInquiries()
        }
    }

    // MARK: - Header

    private var headerView: some View {
        HStack {
            Button(action: handleBack) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }
            Spacer()
            Text(headerTitle)
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

    private var headerTitle: String {
        switch screenState {
        case .main: return "문의사항"
        case .write: return "문의 작성"
        case .detail: return "상세 문의내역"
        }
    }

    private func handleBack() {
        switch screenState {
        case .main:
            dismiss()
        default:
            screenState = .main
        }
    }

    // MARK: - Main Content

    @ViewBuilder
    private var mainContent: some View {
        if viewModel.uiState.isLoading {
            VStack {
                Spacer()
                ProgressView()
                Spacer()
            }
        } else if let error = viewModel.uiState.errorMessage {
            VStack {
                Spacer()
                Text(error)
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.error)
                Spacer()
            }
        } else if viewModel.uiState.inquiries.isEmpty {
            emptyView
        } else {
            ZStack(alignment: .bottomTrailing) {
                inquiryListView
                fabButton
            }
        }
    }

    // MARK: - Empty View

    private var emptyView: some View {
        VStack {
            Spacer()
            Text("아직 등록된 문의가 없어요")
                .font(.system(size: 16))
                .foregroundStyle(AppColors.onBackground.opacity(0.6))
            Spacer()
            Button(action: { screenState = .write }) {
                Text("문의 작성하기")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(AppColors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(20)
        }
    }

    // MARK: - Inquiry List

    private var inquiryListView: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(viewModel.uiState.inquiries.map { $0.toUiItem() }) { item in
                    Button(action: { screenState = .detail(item) }) {
                        inquiryCard(item)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
        }
    }

    private func inquiryCard(_ item: InquiryItem) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                // Status badge
                Text(item.answer != nil ? "답변 완료" : "답변 대기")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(item.answer != nil ? AppColors.primary : AppColors.onSurfaceVariant)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(
                        item.answer != nil
                            ? AppColors.primary.opacity(0.15)
                            : AppColors.onSurfaceVariant.opacity(0.12)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 6))

                Spacer()

                Text(item.date)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurfaceVariant)
            }

            Spacer().frame(height: 12)

            Text(item.title)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(AppColors.onSurface)
                .lineLimit(1)
        }
        .padding(24)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }

    // MARK: - FAB

    private var fabButton: some View {
        Button(action: { screenState = .write }) {
            Image(systemName: "plus")
                .font(.system(size: 20))
                .foregroundStyle(.white)
                .frame(width: 56, height: 56)
                .background(AppColors.primary)
                .clipShape(Circle())
                .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
        }
        .padding(20)
    }

    // MARK: - Write Content

    private var writeContent: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 16) {
                    // Title card
                    TextField("제목을 입력하세요", text: $inquiryTitle)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                        .padding(16)
                        .background(AppColors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 24))

                    // Content card
                    ZStack(alignment: .bottomTrailing) {
                        TextEditor(text: $inquiryContent)
                            .font(.system(size: 15))
                            .foregroundStyle(AppColors.onSurface)
                            .scrollContentBackground(.hidden)
                            .frame(minHeight: 200)
                            .padding(16)
                            .background(AppColors.surface)
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                            .onChange(of: inquiryContent) { _, newValue in
                                if newValue.count > 1000 {
                                    inquiryContent = String(newValue.prefix(1000))
                                }
                            }

                        Text("\(inquiryContent.count)/1000")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                            .padding(16)
                    }
                }
                .padding(20)
            }

            // Submit button
            Button(action: {
                let combined = buildInquiryContent()
                Task {
                    let success = await viewModel.submitInquiry(content: combined)
                    if success {
                        inquiryTitle = ""
                        inquiryContent = ""
                        screenState = .main
                    }
                }
            }) {
                if viewModel.uiState.isSubmitting {
                    ProgressView()
                        .tint(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(AppColors.primary.opacity(0.6))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                } else {
                    Text("제출하기")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(inquiryContent.isEmpty ? AppColors.primary.opacity(0.4) : AppColors.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .disabled(inquiryContent.isEmpty || viewModel.uiState.isSubmitting)
            .padding(20)
        }
    }

    // MARK: - Detail Content

    private func detailContent(_ item: InquiryItem) -> some View {
        ScrollView {
            VStack(spacing: 20) {
                // Question card
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text("Q")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(AppColors.primary)
                            .clipShape(RoundedRectangle(cornerRadius: 4))

                        Text("나의 문의")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundStyle(AppColors.onSurfaceVariant)

                        Spacer()

                        Text(item.date)
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                    }

                    Spacer().frame(height: 16)

                    Text(item.title)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    Spacer().frame(height: 12)

                    Text(item.content)
                        .font(.system(size: 15))
                        .lineSpacing(6)
                        .foregroundStyle(AppColors.onSurface.opacity(0.8))
                }
                .padding(24)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 24))

                // Answer card
                VStack(alignment: .leading, spacing: 0) {
                    HStack {
                        Text("A")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(AppColors.surface)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(
                                item.answer != nil
                                    ? AppColors.onSurface
                                    : AppColors.onSurfaceVariant.opacity(0.5)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 4))

                        Text("관리자 답변")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                    }

                    Spacer().frame(height: 16)

                    if let answer = item.answer {
                        Text(answer)
                            .font(.system(size: 15))
                            .lineSpacing(6)
                            .foregroundStyle(AppColors.onSurface)
                    } else {
                        VStack(spacing: 4) {
                            Text("아직 답변이 등록되지 않았습니다.")
                                .font(.system(size: 14))
                                .foregroundStyle(AppColors.onSurfaceVariant)
                            Text("조금만 기다려주세요!")
                                .font(.system(size: 12))
                                .foregroundStyle(AppColors.onSurfaceVariant.opacity(0.7))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                    }
                }
                .padding(24)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 24))

                Spacer().frame(height: 20)
            }
            .padding(20)
        }
    }

    // MARK: - Helpers

    private func buildInquiryContent() -> String {
        let trimmedTitle = inquiryTitle.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedContent = inquiryContent.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedTitle.isEmpty {
            return trimmedContent
        }
        return "\(trimmedTitle)\n\(trimmedContent)"
    }
}

// MARK: - InquiryModel → InquiryItem

private extension InquiryModel {
    func toUiItem() -> InquiryItem {
        let (title, body) = splitTitleAndBody(content)
        let displayDate = formatInquiryDate(createdAt)
        return InquiryItem(
            id: id,
            title: title,
            content: body,
            date: displayDate,
            answer: answer
        )
    }

    private func splitTitleAndBody(_ content: String) -> (String, String) {
        let lines = content.components(separatedBy: "\n")
        let title = lines.first?.trimmingCharacters(in: .whitespaces).isEmpty == false
            ? lines.first! : String(content.prefix(40))
        let body = lines.count > 1
            ? lines.dropFirst().joined(separator: "\n").trimmingCharacters(in: .whitespacesAndNewlines)
            : content
        return (title, body)
    }

    private func formatInquiryDate(_ raw: String?) -> String {
        guard let raw = raw else {
            let formatter = DateFormatter()
            formatter.dateFormat = "MM/dd HH:mm"
            formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
            return formatter.string(from: Date())
        }

        let inputFormats = [
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS"
        ]

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "MM/dd HH:mm"
        outputFormatter.timeZone = TimeZone(identifier: "Asia/Seoul")

        for format in inputFormats {
            let inputFormatter = DateFormatter()
            inputFormatter.dateFormat = format
            inputFormatter.locale = Locale(identifier: "en_US_POSIX")
            inputFormatter.timeZone = TimeZone(identifier: "Asia/Seoul")

            if let date = inputFormatter.date(from: raw) {
                return outputFormatter.string(from: date)
            }
        }

        return raw
    }
}
