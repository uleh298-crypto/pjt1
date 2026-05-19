import SwiftUI

struct SelectMyGroupView: View {
    var onSelect: (GroupKind) -> Void = { _ in }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                Text("나의 그룹")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
            }
            .padding()
            .background(AppColors.surface)

            VStack(alignment: .leading, spacing: 24) {
                Spacer().frame(height: 8)

                Text("어떤 그룹을 확인할까요?")
                    .font(.title2)
                    .fontWeight(.heavy)
                    .foregroundStyle(AppColors.onBackground)

                Text("프로젝트와 스터디 중 하나만 선택해 주세요.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.7))

                Spacer().frame(height: 8)

                // Project Card
                SelectMyGroupCard(
                    title: "프로젝트",
                    subtitle: "아이디어부터 협업까지 함께",
                    iconName: "person.3.fill",
                    gradient: LinearGradient(
                        colors: [Color(hex: "9FD7FF"), Color(hex: "B59CFF")],
                        startPoint: .leading,
                        endPoint: .trailing
                    ),
                    onTap: { onSelect(.project) }
                )

                // Study Card
                SelectMyGroupCard(
                    title: "스터디",
                    subtitle: "알고리즘 · CS · 면접 준비까지",
                    iconName: "book.fill",
                    gradient: LinearGradient(
                        colors: [Color(hex: "8FD5FF"), Color(hex: "6BA9FF")],
                        startPoint: .leading,
                        endPoint: .trailing
                    ),
                    onTap: { onSelect(.study) }
                )

                Spacer()
            }
            .padding(.horizontal, 20)
        }
        .background(AppColors.background)
    }
}

// MARK: - Select My Group Card

private struct SelectMyGroupCard: View {
    let title: String
    let subtitle: String
    let iconName: String
    let gradient: LinearGradient
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundStyle(.white)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(.white.opacity(0.95))
                }

                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.92))

                HStack {
                    Spacer()
                    Image(systemName: iconName)
                        .font(.system(size: 36))
                        .foregroundStyle(.white)
                }
                .padding(.top, 4)
            }
            .padding(12)
            .background(gradient)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: .black.opacity(0.1), radius: 3, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Hex Color Extension

private extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
