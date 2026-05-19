import Foundation

// MARK: - Constants

private let MEMORY_CACHE_MAX_BYTES = 100 * 1024 * 1024 // 100MB
private let DISK_CACHE_MAX_BYTES = 500 * 1024 * 1024 // 500MB
private let DISK_CACHE_MAX_COUNT = 50

// MARK: - Cache Entry for Disk

private struct CacheEntry: Codable {
    let url: String
    let fileName: String
    let sizeBytes: Int
    var lastAccessed: Date
}

// MARK: - ImageRepositoryImpl

final class ImageRepositoryImpl: ImageRepository {
    static let shared = ImageRepositoryImpl()

    private let memoryCache = NSCache<NSString, NSData>()
    private let fileManager = FileManager.default
    private let cacheDirectory: URL
    private let metadataFile: URL
    private let queue = DispatchQueue(label: "com.ssabree.imagecache", qos: .utility)

    private var metadata: [String: CacheEntry] = [:]

    init() {
        // Setup cache directory
        let cacheDir = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first!
        self.cacheDirectory = cacheDir.appendingPathComponent("ImageCache", isDirectory: true)
        self.metadataFile = cacheDirectory.appendingPathComponent("metadata.json")

        // Create cache directory if needed
        try? fileManager.createDirectory(at: cacheDirectory, withIntermediateDirectories: true)

        // Configure memory cache
        memoryCache.totalCostLimit = MEMORY_CACHE_MAX_BYTES

        // Load metadata
        loadMetadata()
    }

    // MARK: - Public API

    func load(url: String) async -> Result<Data, Error> {
        // 1) 메모리 캐시 확인
        if let cachedData = memoryCache.object(forKey: url as NSString) {
            return .success(cachedData as Data)
        }

        // 2) 디스크 캐시 확인
        if let diskData = await loadFromDisk(url: url) {
            memoryCache.setObject(diskData as NSData, forKey: url as NSString, cost: diskData.count)
            return .success(diskData)
        }

        // 3) 원격 다운로드
        do {
            let data = try await fetchRemote(url: url)

            // 메모리 캐시에 저장
            memoryCache.setObject(data as NSData, forKey: url as NSString, cost: data.count)

            // 디스크 캐시에 저장
            await saveToDisk(url: url, data: data)

            return .success(data)
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Private Methods

    private func loadMetadata() {
        guard fileManager.fileExists(atPath: metadataFile.path) else { return }

        do {
            let data = try Data(contentsOf: metadataFile)
            let entries = try JSONDecoder().decode([CacheEntry].self, from: data)
            metadata = Dictionary(uniqueKeysWithValues: entries.map { ($0.url, $0) })
        } catch {
            print("[ImageCache] Failed to load metadata: \(error)")
            metadata = [:]
        }
    }

    private func saveMetadata() {
        do {
            let entries = Array(metadata.values)
            let data = try JSONEncoder().encode(entries)
            try data.write(to: metadataFile)
        } catch {
            print("[ImageCache] Failed to save metadata: \(error)")
        }
    }

    private func loadFromDisk(url: String) async -> Data? {
        return await withCheckedContinuation { continuation in
            queue.async { [weak self] in
                guard let self = self,
                      var entry = self.metadata[url] else {
                    continuation.resume(returning: nil)
                    return
                }

                let filePath = self.cacheDirectory.appendingPathComponent(entry.fileName)

                guard let data = try? Data(contentsOf: filePath) else {
                    // 파일이 없으면 메타데이터에서도 제거
                    self.metadata.removeValue(forKey: url)
                    self.saveMetadata()
                    continuation.resume(returning: nil)
                    return
                }

                // Update last accessed time
                entry.lastAccessed = Date()
                self.metadata[url] = entry
                self.saveMetadata()

                continuation.resume(returning: data)
            }
        }
    }

    private func saveToDisk(url: String, data: Data) async {
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            queue.async { [weak self] in
                guard let self = self else {
                    continuation.resume()
                    return
                }

                // Generate unique file name
                let fileName = UUID().uuidString

                let filePath = self.cacheDirectory.appendingPathComponent(fileName)

                do {
                    try data.write(to: filePath)

                    let entry = CacheEntry(
                        url: url,
                        fileName: fileName,
                        sizeBytes: data.count,
                        lastAccessed: Date()
                    )

                    self.metadata[url] = entry
                    self.saveMetadata()

                    // Trim cache if needed
                    self.trimDiskCache()
                } catch {
                    print("[ImageCache] Failed to save to disk: \(error)")
                }

                continuation.resume()
            }
        }
    }

    private func trimDiskCache() {
        // Calculate total size and count
        let totalSize = metadata.values.reduce(0) { $0 + $1.sizeBytes }
        let count = metadata.count

        guard totalSize > DISK_CACHE_MAX_BYTES || count > DISK_CACHE_MAX_COUNT else { return }

        // Sort by last accessed (oldest first)
        let sortedEntries = metadata.values.sorted { $0.lastAccessed < $1.lastAccessed }

        var currentSize = totalSize
        var currentCount = count
        var urlsToRemove: [String] = []

        for entry in sortedEntries {
            if currentSize <= DISK_CACHE_MAX_BYTES && currentCount <= DISK_CACHE_MAX_COUNT {
                break
            }

            urlsToRemove.append(entry.url)
            currentSize -= entry.sizeBytes
            currentCount -= 1
        }

        // Delete files and update metadata
        for url in urlsToRemove {
            if let entry = metadata[url] {
                let filePath = cacheDirectory.appendingPathComponent(entry.fileName)
                try? fileManager.removeItem(at: filePath)
                metadata.removeValue(forKey: url)
            }
        }

        saveMetadata()
    }

    private func fetchRemote(url: String) async throws -> Data {
        guard let urlObj = URL(string: url) else {
            throw ImageCacheError.invalidURL
        }

        let (data, response) = try await URLSession.shared.data(from: urlObj)

        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw ImageCacheError.networkError
        }

        return data
    }
}

// MARK: - Error

enum ImageCacheError: Error {
    case invalidURL
    case networkError
    case noData
}

// MARK: - Fake Repository

final class FakeImageRepository: ImageRepository {
    func load(url: String) async -> Result<Data, Error> {
        .failure(ImageCacheError.noData)
    }
}
