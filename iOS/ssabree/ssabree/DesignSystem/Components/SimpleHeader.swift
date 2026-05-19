import SwiftUI

struct SimpleHeader: View {
    let title: String
    
    var body: some View {
        HStack {
            Text(title)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onBackground)
            Spacer()
        }
        .padding()
        .background(AppColors.surface)
    }
}
