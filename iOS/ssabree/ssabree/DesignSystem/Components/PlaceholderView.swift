import SwiftUI

struct PlaceholderView: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "hammer")
                .font(.system(size: 42, weight: .bold))
                .foregroundStyle(AppColors.primary)
            Text(title)
                .font(.title3.weight(.bold))
                .foregroundStyle(AppColors.onSurface)
            Text(subtitle)
                .font(.subheadline)
                .multilineTextAlignment(.center)
                .foregroundStyle(AppColors.onSurface.opacity(0.7))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
        .background(AppColors.background.ignoresSafeArea())
    }
}
