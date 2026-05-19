import Foundation

/// 이미지 캐싱 Repository
/// 순서: 메모리 캐시 -> 로컬 디스크 -> 원격 다운로드
protocol ImageRepository {
    /// URL로부터 이미지 데이터를 가져온다.
    func load(url: String) async -> Result<Data, Error>
}
