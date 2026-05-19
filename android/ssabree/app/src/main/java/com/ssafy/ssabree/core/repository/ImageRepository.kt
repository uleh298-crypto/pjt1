package com.ssafy.ssabree.core.repository

interface ImageRepository {
    /**
        * url로부터 이미지를 가져온다.
        * 순서: 메모리 캐시 -> 로컬 DB -> 원격 다운로드
        */
    suspend fun load(url: String): Result<ByteArray>
}
