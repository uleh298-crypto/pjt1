import SwiftUI

/// 이미지 캐싱을 지원하는 AsyncImage 대체 컴포넌트
/// 메모리 -> 디스크 -> 네트워크 순으로 이미지를 로드
struct CachedAsyncImage<Content: View, Placeholder: View>: View {
    let url: String?
    let imageRepository: ImageRepository
    let contentMode: ContentMode
    let content: (Image) -> Content
    let placeholder: () -> Placeholder

    @State private var imageData: Data?
    @State private var isLoading = false
    @State private var loadFailed = false

    init(
        url: String?,
        imageRepository: ImageRepository = ImageRepositoryImpl.shared,
        contentMode: ContentMode = .fill,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) {
        self.url = url
        self.imageRepository = imageRepository
        self.contentMode = contentMode
        self.content = content
        self.placeholder = placeholder
    }

    var body: some View {
        Group {
            if let data = imageData, let uiImage = UIImage(data: data) {
                content(Image(uiImage: uiImage))
            } else if loadFailed {
                // Error placeholder
                Rectangle()
                    .fill(Color(hex: 0xCCCCCC))
            } else {
                // Loading placeholder
                placeholder()
            }
        }
        .task(id: url) {
            await loadImage()
        }
    }

    private func loadImage() async {
        guard let url = url, !url.isEmpty else {
            loadFailed = true
            return
        }

        guard !isLoading else { return }
        isLoading = true
        loadFailed = false

        let result = await imageRepository.load(url: url)

        switch result {
        case .success(let data):
            imageData = data
            loadFailed = false
        case .failure:
            imageData = nil
            loadFailed = true
        }

        isLoading = false
    }
}

// MARK: - Convenience initializers

extension CachedAsyncImage where Content == Image, Placeholder == Color {
    init(
        url: String?,
        imageRepository: ImageRepository = ImageRepositoryImpl.shared,
        contentMode: ContentMode = .fill
    ) {
        self.init(
            url: url,
            imageRepository: imageRepository,
            contentMode: contentMode,
            content: { $0.resizable() },
            placeholder: { Color(hex: 0xE9E9E9) }
        )
    }
}

extension CachedAsyncImage where Placeholder == Color {
    init(
        url: String?,
        imageRepository: ImageRepository = ImageRepositoryImpl.shared,
        contentMode: ContentMode = .fill,
        @ViewBuilder content: @escaping (Image) -> Content
    ) {
        self.init(
            url: url,
            imageRepository: imageRepository,
            contentMode: contentMode,
            content: content,
            placeholder: { Color(hex: 0xE9E9E9) }
        )
    }
}

// MARK: - Color Extension for Hex

private extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }
}
