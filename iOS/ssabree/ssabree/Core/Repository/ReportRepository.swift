import Foundation

// MARK: - Report Target Type

enum ReportTargetType: String {
    case post = "POST"
    case comment = "COMMENT"
    case user = "USER"
}

// MARK: - Report Reason

enum ReportReason: String, CaseIterable {
    case abuse = "ABUSE"
    case spam = "SPAM"
    case inappropriate = "INAPPROPRIATE"
    case other = "OTHER"

    var label: String {
        switch self {
        case .abuse: return "욕설/비방"
        case .spam: return "스팸/광고"
        case .inappropriate: return "부적절한 내용"
        case .other: return "기타"
        }
    }
}

// MARK: - Report Repository

protocol ReportRepository {
    func createReport(targetType: ReportTargetType, targetId: Int, reason: ReportReason, detail: String?) async -> Result<Void, Error>
}

final class ReportRepositoryImpl: ReportRepository {
    private let reportService: ReportService

    init(reportService: ReportService) {
        self.reportService = reportService
    }

    func createReport(targetType: ReportTargetType, targetId: Int, reason: ReportReason, detail: String?) async -> Result<Void, Error> {
        do {
            let request = ReportCreateRequest(
                targetType: targetType.rawValue,
                targetId: targetId,
                reason: reason.rawValue,
                detail: detail
            )
            print("[ReportRepository] Calling API with request: targetType=\(request.targetType), targetId=\(request.targetId), reason=\(request.reason)")
            try await reportService.createReport(request)
            print("[ReportRepository] API call succeeded")
            return .success(())
        } catch {
            print("[ReportRepository] API call failed: \(error)")
            return .failure(error)
        }
    }
}

final class FakeReportRepository: ReportRepository {
    func createReport(targetType: ReportTargetType, targetId: Int, reason: ReportReason, detail: String?) async -> Result<Void, Error> {
        .success(())
    }
}
