package uts.sdk.modules.uniFaceAISDK

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.ai.face.base.addFace.AddFaceCallBack
import com.ai.face.base.addFace.AddFaceDispose
import com.ai.face.base.utils.DataConvertUtils
import com.ai.face.faceVerify.verify.VerifyStatus
import com.faceAI.demo.FaceSDKConfig
import com.faceAI.demo.R
import com.faceAI.demo.base.utils.BitmapUtils
import com.faceAI.demo.base.view.FaceCoverView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CaptureFaceActivity、UTS 标准模式组件和兼容模式组件共用的原生 View。
 *
 * View 自己持有 CameraX 预览、帧分析和人脸抓拍生命周期，避免组件入口依赖
 * FragmentContainerView 或宿主页面的 FragmentManager。
 */
@Keep
class CaptureFaceNativeView(context: Context) : FrameLayout(context) {

    private val previewView = PreviewView(context)
    private val faceCoverView = FaceCoverView(context)
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val resultExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val encodingResult = AtomicBoolean(false)

    private var cameraProvider: ProcessCameraProvider? = null
    private var faceDispose: AddFaceDispose? = null
    private var resultCallback: ((String, Float, String) -> Unit)? = null
    private var tipsCallback: ((Int, String) -> Unit)? = null
    private var errorCallback: ((String, String) -> Unit)? = null

    private var performanceMode = AddFaceDispose.PERFORMANCE_MODE_FAST
    private var needLivenessCheck = true
    private var cameraId = CameraSelector.LENS_FACING_FRONT
    private var linearZoom = 0.12f
    private var rotationDegrees = 0
    private var cameraSizeHigh = false
    private var started = false
    private var released = false
    private var sessionId = 0L

    init {
        setBackgroundColor(Color.BLACK)

        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
        addView(
            previewView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        addView(
            faceCoverView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    fun setResultCallback(callback: ((String, Float, String) -> Unit)?) {
        resultCallback = callback
    }

    fun setTipsCallback(callback: ((Int, String) -> Unit)?) {
        tipsCallback = callback
    }

    fun setErrorCallback(callback: ((String, String) -> Unit)?) {
        errorCallback = callback
    }

    /**
     * 开始持续抓拍。重复调用会先结束上一轮，再使用新参数重新绑定相机。
     */
    @JvmOverloads
    fun start(
        performanceMode: Int = AddFaceDispose.PERFORMANCE_MODE_FAST,
        needLivenessCheck: Boolean = true,
        cameraId: Int = CameraSelector.LENS_FACING_FRONT,
        linearZoom: Float = 0.12f,
        rotationDegrees: Int = 0,
        cameraSizeHigh: Boolean = false
    ) {
        if (released) {
            notifyError("VIEW_RELEASED", "CaptureFaceNativeView has been released")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifyError("CAMERA_PERMISSION_REQUIRED", "Camera permission is required")
            return
        }

        val lifecycleOwner = findActivity() as? LifecycleOwner
        if (lifecycleOwner == null) {
            notifyError("LIFECYCLE_OWNER_REQUIRED", "The component host must implement LifecycleOwner")
            return
        }

        stopInternal()
        this.performanceMode = performanceMode.coerceIn(
            AddFaceDispose.PERFORMANCE_MODE_NO_LIMIT,
            AddFaceDispose.PERFORMANCE_MODE_ACCURATE
        )
        this.needLivenessCheck = needLivenessCheck
        this.cameraId = cameraId
        this.linearZoom = linearZoom.coerceIn(0f, 1f)
        this.rotationDegrees = rotationDegrees
        this.cameraSizeHigh = cameraSizeHigh
        this.started = true
        val currentSession = ++sessionId

        // API、标准组件、兼容组件都可以独立作为第一个插件入口使用。
        FaceSDKConfig.init(context)
        faceDispose = AddFaceDispose(
            context,
            this.performanceMode,
            this.needLivenessCheck,
            object : AddFaceCallBack() {
                override fun onCompleted(
                    cropped: android.graphics.Bitmap,
                    silentScore: Float,
                    origin: android.graphics.Bitmap
                ) {
                    encodeAndDispatch(currentSession, cropped, silentScore, origin)
                }

                override fun onProcessTips(actionCode: Int) {
                    post {
                        if (started && !released && currentSession == sessionId) {
                            showProcessTips(actionCode)
                        }
                    }
                }
            }
        )

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            if (!started || released || currentSession != sessionId) {
                return@addListener
            }
            try {
                cameraProvider = providerFuture.get()
                bindCamera(lifecycleOwner, currentSession)
            } catch (e: Exception) {
                notifyError("CAMERA_INIT_FAILED", e.message ?: "Camera initialization failed")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        if (released) return
        stopInternal()
    }

    fun retry() {
        if (started && !released) {
            faceDispose?.retry()
        }
    }

    fun switchCamera(newCameraId: Int) {
        if (released || newCameraId == cameraId) return
        start(
            performanceMode,
            needLivenessCheck,
            newCameraId,
            linearZoom,
            rotationDegrees,
            cameraSizeHigh
        )
    }

    fun release() {
        if (released) return
        stopInternal()
        released = true
        resultCallback = null
        tipsCallback = null
        errorCallback = null
        analysisExecutor.shutdownNow()
        resultExecutor.shutdownNow()
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    private fun bindCamera(lifecycleOwner: LifecycleOwner, currentSession: Long) {
        val provider = cameraProvider ?: return
        val surfaceRotation = toSurfaceRotation(rotationDegrees)

        val preview = Preview.Builder()
            .setTargetRotation(surfaceRotation)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val analysisBuilder = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetRotation(surfaceRotation)

        if (cameraSizeHigh) {
            analysisBuilder.setTargetResolution(android.util.Size(1280, 720))
        } else {
            analysisBuilder.setTargetAspectRatio(AspectRatio.RATIO_4_3)
        }

        val imageAnalysis = analysisBuilder.build()
        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
            try {
                if (started && !released && currentSession == sessionId && !encodingResult.get()) {
                    faceDispose?.dispose(DataConvertUtils.imageProxy2Bitmap(imageProxy))
                }
            } catch (e: Exception) {
                notifyError("FRAME_PROCESS_FAILED", e.message ?: "Camera frame processing failed")
            } finally {
                imageProxy.close()
            }
        }

        try {
            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                createCompatibleCameraSelector(provider, cameraId),
                preview,
                imageAnalysis
            )
            camera.cameraControl.setLinearZoom(linearZoom)
        } catch (e: Exception) {
            notifyError("CAMERA_BIND_FAILED", e.message ?: "Camera bind failed")
        }
    }

    private fun encodeAndDispatch(
        currentSession: Long,
        cropped: android.graphics.Bitmap,
        silentScore: Float,
        origin: android.graphics.Bitmap
    ) {
        if (!encodingResult.compareAndSet(false, true)) return

        resultExecutor.execute {
            try {
                val croppedBase64 = BitmapUtils.bitmapToBase64(cropped)
                val originBase64 = BitmapUtils.bitmapToBase64(origin)
                post {
                    if (started && !released && currentSession == sessionId) {
                        try {
                            resultCallback?.invoke(croppedBase64, silentScore, originBase64)
                        } catch (e: Exception) {
                            Log.e(TAG, "Result callback failed", e)
                        } finally {
                            // 即使业务回调抛出异常，也要重新开放检测，保持持续结果流。
                            faceDispose?.retry()
                        }
                    }
                }
            } catch (e: Exception) {
                notifyError("BITMAP_ENCODE_FAILED", e.message ?: "Bitmap Base64 encode failed")
                post {
                    if (started && !released && currentSession == sessionId) {
                        faceDispose?.retry()
                    }
                }
            } finally {
                encodingResult.set(false)
            }
        }
    }

    private fun stopInternal() {
        started = false
        sessionId++
        encodingResult.set(false)
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {
        }
        cameraProvider = null
        faceDispose?.release()
        faceDispose = null
    }

    private fun showProcessTips(actionCode: Int) {
        val textRes = when (actionCode) {
            VerifyStatus.VERIFY_DETECT_TIPS_ENUM.NO_FACE_REPEATEDLY -> R.string.no_face_detected_tips
            VerifyStatus.VERIFY_DETECT_TIPS_ENUM.FACE_TOO_SMALL -> R.string.come_closer_tips
            VerifyStatus.VERIFY_DETECT_TIPS_ENUM.FACE_TOO_LARGE -> R.string.far_away_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.CLOSE_EYE -> R.string.no_close_eye_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.HEAD_CENTER -> R.string.keep_face_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.TILT_HEAD -> R.string.no_tilt_head_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.HEAD_LEFT -> R.string.head_turn_left_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.HEAD_RIGHT -> R.string.head_turn_right_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.HEAD_UP -> R.string.no_look_up_tips
            VerifyStatus.ALIVE_DETECT_TYPE_ENUM.HEAD_DOWN -> R.string.no_look_down_tips
            else -> 0
        }

        val message = if (textRes != 0) context.getString(textRes) else "Tips Code: $actionCode"
        if (textRes != 0) {
            faceCoverView.setTipsText(textRes)
        }
        try {
            tipsCallback?.invoke(actionCode, message)
        } catch (e: Exception) {
            Log.e(TAG, "Tips callback failed", e)
        }
    }

    private fun createCompatibleCameraSelector(
        provider: ProcessCameraProvider,
        preferredLensFacing: Int
    ): CameraSelector {
        val fallbackLensFacing = if (preferredLensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }

        return when {
            hasCamera(provider, preferredLensFacing) -> CameraSelector.Builder()
                .requireLensFacing(preferredLensFacing)
                .build()

            hasCamera(provider, fallbackLensFacing) -> CameraSelector.Builder()
                .requireLensFacing(fallbackLensFacing)
                .build()

            else -> CameraSelector.Builder()
                .addCameraFilter { cameraInfos -> cameraInfos }
                .build()
        }
    }

    private fun hasCamera(provider: ProcessCameraProvider, lensFacing: Int): Boolean {
        return try {
            provider.hasCamera(
                CameraSelector.Builder().requireLensFacing(lensFacing).build()
            )
        } catch (_: Exception) {
            false
        }
    }

    private fun toSurfaceRotation(value: Int): Int {
        return when (value) {
            90 -> Surface.ROTATION_90
            180 -> Surface.ROTATION_180
            270 -> Surface.ROTATION_270
            else -> Surface.ROTATION_0
        }
    }

    private fun findActivity(): Activity? {
        var currentContext: Context? = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return currentContext as? Activity
    }

    private fun notifyError(code: String, message: String) {
        post {
            try {
                errorCallback?.invoke(code, message)
            } catch (e: Exception) {
                Log.e(TAG, "Error callback failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "CaptureFaceNativeView"
    }
}
