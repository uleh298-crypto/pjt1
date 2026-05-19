import SwiftUI

// MARK: - Report Dialog

struct ReportDialog: View {
    @Binding var isPresented: Bool
    let targetType: ReportTargetType
    let targetId: Int
    let onReport: (ReportReason, String?) async -> Void

    @State private var selectedReason: ReportReason = .abuse
    @State private var detailText: String = ""
    @State private var showConfirmDialog = false
    @State private var isSubmitting = false

    var body: some View {
        ZStack {
            // Background dimming
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture {
                    if !isSubmitting {
                        isPresented = false
                    }
                }

            // Dialog content
            VStack(spacing: 0) {
                // Title
                Text("신고")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)
                    .padding(.top, 24)
                    .padding(.bottom, 16)

                ScrollView {
                    VStack(spacing: 0) {
                        // Reason selection
                        VStack(alignment: .leading, spacing: 12) {
                            Text("신고 사유")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundStyle(AppColors.onSurface)
                                .padding(.horizontal, 24)

                            ForEach(ReportReason.allCases, id: \.rawValue) { reason in
                                ReportReasonRow(
                                    reason: reason,
                                    isSelected: selectedReason == reason,
                                    onSelect: { selectedReason = reason }
                                )
                            }
                        }
                        .padding(.bottom, 16)

                        // Detail text field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("상세 내용 (선택)")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundStyle(AppColors.onSurface)

                            TextField("신고 사유를 자세히 입력해주세요", text: $detailText, axis: .vertical)
                                .font(.system(size: 14))
                                .padding(12)
                                .lineLimit(3...5)
                                .background(AppColors.surfaceVariant.opacity(0.5))
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                        .padding(.horizontal, 24)
                        .padding(.bottom, 24)
                    }
                }
                .scrollDismissesKeyboard(.interactively)

                Divider()

                // Buttons
                HStack(spacing: 0) {
                    Button(action: {
                        isPresented = false
                    }) {
                        Text("취소")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                    }
                    .disabled(isSubmitting)

                    Divider()
                        .frame(height: 48)

                    Button(action: {
                        showConfirmDialog = true
                    }) {
                        if isSubmitting {
                            ProgressView()
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                        } else {
                            Text("신고하기")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundStyle(AppColors.error)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                        }
                    }
                    .disabled(isSubmitting)
                }
            }
            .frame(maxHeight: UIScreen.main.bounds.height * 0.7)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 32)
        }
        .alert("신고 확인", isPresented: $showConfirmDialog) {
            Button("취소", role: .cancel) {}
            Button("신고", role: .destructive) {
                Task {
                    await submitReport()
                }
            }
        } message: {
            Text("해당 \(targetTypeLabel)을(를) 신고하시겠습니까?\n신고된 내용은 검토 후 조치됩니다.")
        }
    }

    private var targetTypeLabel: String {
        switch targetType {
        case .post: return "게시글"
        case .comment: return "댓글"
        case .user: return "사용자"
        }
    }

    private func submitReport() async {
        print("[ReportDialog] submitReport called - reason: \(selectedReason.rawValue), targetType: \(targetType.rawValue), targetId: \(targetId)")
        isSubmitting = true
        let detail = detailText.trimmingCharacters(in: .whitespacesAndNewlines)
        print("[ReportDialog] Calling onReport callback...")
        await onReport(selectedReason, detail.isEmpty ? nil : detail)
        print("[ReportDialog] onReport callback completed")
        isSubmitting = false
        isPresented = false
    }
}

// MARK: - Report Reason Row

private struct ReportReasonRow: View {
    let reason: ReportReason
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 12) {
                Image(systemName: isSelected ? "circle.inset.filled" : "circle")
                    .font(.system(size: 20))
                    .foregroundStyle(isSelected ? AppColors.primary : AppColors.onSurface.opacity(0.4))

                Text(reason.label)
                    .font(.system(size: 15))
                    .foregroundStyle(AppColors.onSurface)

                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)
        }
        .buttonStyle(.plain)
    }
}
