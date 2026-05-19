import SwiftUI

struct ContentView: View {
    let container: AppContainer
    @Environment(\.themeManager) private var themeManager

    var body: some View {
        RootView(container: container)
            .preferredColorScheme(themeManager.preferredColorScheme)
    }
}

#Preview {
    ContentView(container: FakeAppContainer())
}
