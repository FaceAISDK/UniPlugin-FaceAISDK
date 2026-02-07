package uts.sdk.modules.uniFaceAISDK

/**
 * 全局单例管理器，用于连接 Activity 和 UTS
 * 修改说明：回调增加 liveness 参数
 */
object FaceResultManager {

    // 修改回调签名，增加 Float 参数
    private var internalCallback: ((String, Float,String) -> Unit)? = null

    fun setCallback(cb: (String, Float,String) -> Unit) {
        this.internalCallback = cb
    }

    // 发送结果时携带 livenessValue
    fun sendResult(json: String, liveness: Float,base64:String) {
        internalCallback?.invoke(json, liveness,base64)
    }
}