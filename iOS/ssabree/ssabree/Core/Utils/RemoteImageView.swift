import SwiftUI
import WebKit

/// SVG와 일반 이미지(PNG/JPEG) 모두 로드 가능한 원격 이미지 뷰
/// - SVG URL (.svg): WKWebView 기반 렌더링
/// - 일반 URL: AsyncImage 사용
struct RemoteImageView: View {
    let url: String
    let size: CGFloat

    var body: some View {
        if url.lowercased().hasSuffix(".svg") {
            SVGWebView(url: url)
                .frame(width: size, height: size)
        } else {
            AsyncImage(url: URL(string: url)) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Color.clear
            }
            .frame(width: size, height: size)
        }
    }
}

/// WKWebView 기반 SVG 렌더러
private struct SVGWebView: UIViewRepresentable {
    let url: String

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        webView.isUserInteractionEnabled = false
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        let html = """
        <html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
        *{margin:0;padding:0}
        body{background:transparent;display:flex;justify-content:center;align-items:center;width:100%;height:100vh;overflow:hidden}
        img{max-width:100%;max-height:100%;object-fit:contain}
        </style>
        </head><body><img src="\(url)"></body></html>
        """
        webView.loadHTMLString(html, baseURL: nil)
    }
}
