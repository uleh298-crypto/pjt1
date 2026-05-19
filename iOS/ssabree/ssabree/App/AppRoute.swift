import Foundation

enum AppRoute: Hashable {
    case splash
    case login
    case join
    case findIdPass
    case home
    case board
    case boardDetail(boardId: Int, postId: Int)
    case boardWrite(boardId: Int)
    case boardEdit(postId: Int)  // 게시글 수정
    case boardSearch
    case group
    case groupList(kind: GroupKind)
    case groupDetail(kind: GroupKind, id: Int)
    case groupWrite(kind: GroupKind)
    case groupEdit(kind: GroupKind, id: Int)
    case groupApply(groupId: Int, groupKind: GroupKind)
    case selectGroup
    case myGroup
    case myApplications(groupKind: GroupKind)
    case myGroupDetail(groupId: Int, groupKind: GroupKind, isLeader: Bool)
    case groupManage(groupId: Int, groupKind: GroupKind)
    case memberManage(groupId: Int, groupKind: GroupKind)
    case announcements(groupId: Int, groupKind: GroupKind, isLeader: Bool)
    case writeAnnouncement(groupId: Int, groupKind: GroupKind)
    case editAnnouncement(groupId: Int, groupKind: GroupKind, announcementId: Int)
    case addTask(groupId: Int, groupKind: GroupKind)
    case taskDetail(groupId: Int, groupKind: GroupKind, taskId: Int)
    case editTask(groupId: Int, groupKind: GroupKind, taskId: Int)
    case myProgress(id: Int)
    case selectMyGroup
    case myGroupList(kind: GroupKind)
    case message
    case messageDetail(roomId: Int)
    case newChat(postId: Int)  // 게시글에서 쪽지 보내기 (대상은 백엔드에서 게시글 작성자로 결정)
    case ddayDetail
    case myPage
    case myPosts
    case myComments
    case myScraps
    case portfolioDetail
    case applicantPortfolio(portfolioId: Int, applicationId: Int? = nil, groupKind: GroupKind? = nil)
    case applicantPortfolioDetail(portfolioId: Int)
    case projectWrite(portfolioId: Int, projectId: Int?)
    case inquiry
    case setting
    case termsOfService
    case communityRules
    case notification
    case notificationSettings
    case notificationDetail(id: Int)
    case report
}
