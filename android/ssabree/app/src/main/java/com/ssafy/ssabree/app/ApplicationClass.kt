package com.ssafy.ssabree.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.ssafy.ssabree.core.utils.EncryptedSharedPrefManager
import com.ssafy.ssabree.core.utils.AppForegroundTracker
import javax.crypto.AEADBadTagException

class ApplicationClass : Application() {

    companion object {
        lateinit var appContext: Context
            private set
        lateinit var encryptedSharedPrefManager: EncryptedSharedPrefManager
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        encryptedSharedPrefManager = initEncryptedSharedPrefManager()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppForegroundTracker)
    }

    private fun initEncryptedSharedPrefManager(): EncryptedSharedPrefManager {
        return try {
            // 첫 시도
            EncryptedSharedPrefManager(applicationContext)
        } catch (e: AEADBadTagException) {
            // 복호화 실패 → 백업된 암호화 데이터가 깨진 상태
            clearCorruptedEncryptedPrefs(this)
            EncryptedSharedPrefManager(applicationContext)
        } catch (e: Exception) {
            // 혹시 다른 GeneralSecurityException 등도 같이 처리
            clearCorruptedEncryptedPrefs(this)
            EncryptedSharedPrefManager(applicationContext)
        }
    }

    private fun clearCorruptedEncryptedPrefs(context: Context) {
        // EncryptedSharedPreferences가 사용하는 xml 파일 이름과 동일해야 함
        context.deleteSharedPreferences("ssabree_preference")
        // 필요하면 여기서 추가로 로그 찍어도 됨(Log.e 등)
    }
}
