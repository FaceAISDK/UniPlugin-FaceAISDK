package uts.sdk.modules.uniFaceAISDK

/**
 * 全局单例管理器，用于连接 Activity 和 UTS
 */
object FaceResultManager {

    private var internalCallback: ((String) -> Unit)? = null

    fun setCallback(cb: (String) -> Unit) {
        this.internalCallback = cb
    }

    fun sendResult(json: String) {
        internalCallback?.invoke(json)
    }
}