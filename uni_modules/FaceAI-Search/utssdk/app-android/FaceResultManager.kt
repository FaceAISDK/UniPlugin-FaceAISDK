package uts.sdk.modules.uniFaceAISDK

/**
 * 全局单例管理器，用于连接 Activity 和 UTS
 */
object FaceResultManager {

    // 保存回调引用
    private var internalCallback: ((String, Float, String) -> Unit)? = null

    // UTS 中调用此方法设置回调
    fun setCallback(cb: (String, Float, String) -> Unit) {
        this.internalCallback = cb
    }

    // Java/Kotlin 中调用此方法发送结果
    fun sendResult(json: String, liveness: Float, base64: String) {
        internalCallback?.invoke(json, liveness, base64)
    }
}