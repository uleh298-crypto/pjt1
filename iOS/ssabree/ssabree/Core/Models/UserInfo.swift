import Foundation

struct UserInfo {
    let userId: String
    var password: String
    var nickname: String
    var role: String
    var uid: Int

    init(userId: String) {
        self.userId = userId
        self.password = ""
        self.nickname = ""
        self.role = ""
        self.uid = -1
    }

    init(userId: String, password: String) {
        self.userId = userId
        self.password = password
        self.nickname = ""
        self.role = ""
        self.uid = -1
    }

    init(userId: String, password: String, nickname: String, role: String, uid: Int) {
        self.userId = userId
        self.password = password
        self.nickname = nickname
        self.role = role
        self.uid = uid
    }
}
