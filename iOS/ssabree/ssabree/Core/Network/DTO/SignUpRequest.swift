import Foundation

struct SignUpRequest: Encodable {
    let email: String
    let password: String
    let name: String
    let studentNo: Int?
    let campus: Int?
    let generation: Int?
    let classNo: Int?
    let mattermostId: String
}
