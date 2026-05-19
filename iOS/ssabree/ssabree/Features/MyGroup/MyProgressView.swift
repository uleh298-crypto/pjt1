import SwiftUI

struct MyProgressView: View {
    let groupId: Int
    @State var viewModel: MyProgressViewModel
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
                Spacer()
                Text("나의 활동 내역")
                    .font(.headline)
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
                // Empty view for balance
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(.clear)
            }
            .padding()
            .background(AppColors.surface)
            
            ScrollView {
                VStack(spacing: 20) {
                    // Summary Card
                    let attended = viewModel.uiState.progressList.filter { $0.status == "ATTENDED" || $0.status == "출석" }.count
                    let late = viewModel.uiState.progressList.filter { $0.status == "LATE" || $0.status == "지각" }.count
                    let absent = viewModel.uiState.progressList.filter { $0.status == "ABSENT" || $0.status == "결석" }.count
                    
                    HStack(spacing: 0) {
                        SummaryItem(label: "출석", value: "\(attended)", color: .blue)
                        Divider().frame(height: 30)
                        SummaryItem(label: "지각", value: "\(late)", color: .orange)
                        Divider().frame(height: 30)
                        SummaryItem(label: "결석", value: "\(absent)", color: .red)
                    }
                    .padding()
                    .background(AppColors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
                    .padding(.horizontal)
                    
                    // List
                    VStack(alignment: .leading, spacing: 12) {
                        Text("상세 내역")
                            .font(.headline)
                            .foregroundStyle(AppColors.onSurface)
                            .padding(.horizontal)
                        
                        if viewModel.uiState.isLoading {
                            ProgressView().padding()
                        } else if let error = viewModel.uiState.error {
                            Text(error).foregroundStyle(AppColors.error).padding()
                        } else if viewModel.uiState.progressList.isEmpty {
                            Text("활동 내역이 없습니다.").foregroundStyle(Color.gray).padding()
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(viewModel.uiState.progressList) { item in
                                    ProgressRow(item: item)
                                }
                            }
                            .padding(.horizontal)
                        }
                    }
                }
                .padding(.vertical)
            }
            .background(AppColors.background)
        }
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadProgress(groupId: groupId)
        }
    }
}

private struct SummaryItem: View {
    let label: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundStyle(color)
            Text(label)
                .font(.caption)
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
        }
        .frame(maxWidth: .infinity)
    }
}

private struct ProgressRow: View {
    let item: ProgressModel
    
    var color: Color {
        switch item.status {
        case "ATTENDED", "출석": return .blue
        case "LATE", "지각": return .orange
        case "ABSENT", "결석": return .red
        default: return .gray
        }
    }
    
    var statusText: String {
        switch item.status {
        case "ATTENDED": return "출석"
        case "LATE": return "지각"
        case "ABSENT": return "결석"
        default: return item.status
        }
    }
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(item.title)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundStyle(AppColors.onSurface)
                Text(item.date)
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
            }
            
            Spacer()
            
            Text(statusText)
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(color)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(color.opacity(0.1))
                .clipShape(Capsule())
        }
        .padding()
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
