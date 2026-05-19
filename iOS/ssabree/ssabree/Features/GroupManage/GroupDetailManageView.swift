import SwiftUI

struct GroupDetailManageView: View {
    let isLeader: Bool
    let onBackTap: () -> Void
    let onMemberManageTap: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                header
                noticeSection
                memberSection
                progressSection
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
        }
        .background(AppColors.background.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Button(action: onBackTap) {
                    Image(systemName: "chevron.left")
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(AppColors.onSurface)
                }
                Text("나의 그룹")
                    .font(.title3.weight(.semibold))
                Spacer()
            }

            VStack(alignment: .leading, spacing: 12) {
                Text("진행중")
                    .font(.caption.weight(.bold))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(AppColors.primary.opacity(0.15))
                    .foregroundStyle(AppColors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

                Text("자율주행 시뮬레이터 프로젝트")
                    .font(.title3.weight(.bold))

                if isLeader {
                    HStack(spacing: 8) {
                        Button("수정") {}
                            .font(.footnote.weight(.semibold))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(Color(red: 0xA8/255, green: 0xD5/255, blue: 0xBA/255))
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        Button("삭제") {}
                            .font(.footnote.weight(.semibold))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(AppColors.primary)
                            .foregroundStyle(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }

                HStack {
                    statItem(label: "멤버", value: "4/6명")
                    Divider().frame(height: 24)
                    statItem(label: "지원 현황", value: "3명")
                    Divider().frame(height: 24)
                    statItem(label: "D-day", value: "D-42")
                }
                .padding(.vertical, 12)
                .frame(maxWidth: .infinity)
                .background(Color.gray.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(20)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: .black.opacity(0.05), radius: 6, x: 0, y: 3)
        }
    }

    private func statItem(label: String, value: String) -> some View {
        VStack {
            Text(label)
                .font(.caption)
                .foregroundStyle(.gray)
            Text(value)
                .font(.body.weight(.bold))
                .foregroundStyle(AppColors.onSurface)
        }
        .frame(maxWidth: .infinity)
    }

    private var noticeSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("공지사항")
                    .font(.title3.weight(.bold))
                Spacer()
                Text("전체보기")
                    .font(.footnote)
                    .foregroundStyle(.gray)
            }

            VStack(spacing: 0) {
                noticeRow("금주 정기 회의 (26-01-23 19:00)")
                Divider().background(Color.gray.opacity(0.3))
                noticeRow("UI/UX 피드백", isRead: true)
            }
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.05), radius: 3, x: 0, y: 2)
        }
    }

    private func noticeRow(_ text: String, isRead: Bool = false) -> some View {
        HStack(spacing: 12) {
            Image(systemName: "megaphone.fill")
                .foregroundStyle(isRead ? Color.gray.opacity(0.5) : Color.gray)
            Text(text)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(isRead ? Color.gray.opacity(0.6) : AppColors.onSurface)
            Spacer()
        }
        .padding(16)
    }

    private var memberSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("멤버 목록")
                    .font(.title3.weight(.bold))
                Spacer()
                Button {
                    onMemberManageTap()
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "person.fill")
                            .font(.system(size: 12))
                        Text("멤버 관리")
                            .font(.caption)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(AppColors.primary.opacity(0.6))
                    .foregroundStyle(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }

            VStack(spacing: 0) {
                memberRow("김싸피", "팀장 • 14기 • 구미 캠퍼스", isLeader: true)
                Divider().background(Color.gray.opacity(0.3))
                memberRow("이싸피", "멤버 • 14기 • 구미 캠퍼스", isLeader: false)
                Divider().background(Color.gray.opacity(0.3))
                memberRow("박싸피", "멤버 • 14기 • 구미 캠퍼스", isLeader: false)
            }
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: .black.opacity(0.05), radius: 3, x: 0, y: 2)
        }
    }

    private func memberRow(_ name: String, _ info: String, isLeader: Bool) -> some View {
        HStack(spacing: 16) {
            Circle()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 40, height: 40)
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(name)
                        .font(.subheadline.weight(.bold))
                    if isLeader {
                        Text("방장")
                            .font(.caption)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(AppColors.primary.opacity(0.2))
                            .foregroundStyle(AppColors.primary)
                            .clipShape(RoundedRectangle(cornerRadius: 4))
                    }
                }
                Text(info)
                    .font(.caption)
                    .foregroundStyle(.gray)
            }
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    private var progressSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("일일 진행 현황")
                    .font(.title3.weight(.bold))
                Spacer()
                Image(systemName: "plus.circle.fill")
                    .foregroundStyle(Color(red: 0xE9/255, green: 0xE4/255, blue: 0x95/255))
                    .font(.system(size: 24))
            }

            VStack(spacing: 12) {
                progressRow(name: "김싸피", role: "Frontend", desc: "Repository 스켈레톤 코드 작성", status: "완료", color: .gray)
                progressRow(name: "김싸피", role: "Frontend", desc: "Figma 목업 생성", status: "진행", color: Color(red: 0xA8/255, green: 0xD5/255, blue: 0xBA/255))
                progressRow(name: "김싸피", role: "Frontend", desc: "Figma 목업 생성", status: "예정", color: Color(red: 0xF9/255, green: 0xF5/255, blue: 0xD7/255))
            }
        }
    }

    private func progressRow(name: String, role: String, desc: String, status: String, color: Color) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Circle()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 40, height: 40)
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(name).font(.subheadline.weight(.bold))
                        Text(role).font(.caption).foregroundStyle(.gray)
                    }
                    Spacer()
                    Text(status)
                        .font(.caption.weight(.bold))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(color.opacity(0.5))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .foregroundStyle(Color.black.opacity(0.6))
                }
                Text(desc)
                    .font(.footnote)
                    .foregroundStyle(AppColors.onSurface)
            }
            Spacer()
        }
        .padding(14)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 3, x: 0, y: 2)
    }
}
