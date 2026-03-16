package uts.sdk.modules.uniFaceAISDK

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity

// 第三方库引用
import com.ai.face.core.engine.FaceAISDKEngine
import com.ai.face.faceSearch.search.Image2FaceFeature
import com.faceAI.demo.FaceSDKConfig
import com.faceAI.demo.SysCamera.search.ImageToast
import com.faceAI.demo.base.utils.BitmapUtils
import com.faceAI.demo.base.utils.VoicePlayer
import com.tencent.mmkv.MMKV

// UTS/UniApp 引用
import io.dcloud.uts.UTSAndroid
import io.dcloud.uts.UTSJSONObject
import io.dcloud.uts.clearInterval
import io.dcloud.uts.console
import io.dcloud.uts.setInterval
import org.json.JSONObject

 
/**
 *  kotlin 方法集  
 *
 */
object FaceAISDKNative {
	/**
	 * Toast 信息
	 * 
	 */
	fun toastMessage(context:Context,base64Image: String,message: String){
        ImageToast().show(context, base64Image, message)
	}
	
}

