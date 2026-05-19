import SwiftUI

struct SplashView: View {
    let onTimeout: () -> Void

    init(onTimeout: @escaping () -> Void) {
        self.onTimeout = onTimeout
    }

    var body: some View {
        ZStack {
            // Gradient Background (Android: #2F6BFF -> background)
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0x2F / 255, green: 0x6B / 255, blue: 0xFF / 255),
                    AppColors.background
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack {
                // Logo in rounded surface
                ZStack {
                    RoundedRectangle(cornerRadius: 28)
                        .fill(AppColors.surface.opacity(0.95))
                        .frame(width: 120, height: 120)
                        .shadow(color: .black.opacity(0.15), radius: 6, x: 0, y: 4)

                    Image("logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 88, height: 88)
                }

                Spacer().frame(height: 16)

                // Title
                Text("싸브리타임")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                Spacer().frame(height: 4)

                // Subtitle
                Text("SSAFY 캠퍼스 커뮤니티")
                    .font(.system(size: 13))
                    .foregroundStyle(AppColors.onBackground.opacity(0.7))

            }
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                onTimeout()
            }
        }
    }
}
