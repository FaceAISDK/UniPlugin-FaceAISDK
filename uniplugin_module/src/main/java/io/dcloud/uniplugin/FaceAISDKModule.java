package io.dcloud.uniplugin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;


/**
 * 扩展方法必须加上@UniJSMethod (uiThread = false or true) 注解。
 * UniApp 会根据注解来判断当前方法是否要运行在 UI 线程，和当前方法是否是扩展方法。
 *
 * UniApp是根据反射来进行调用 Module 扩展方法，所以Module中的扩展方法必须是 public 类型。
 *
 */
public class FaceAISDKModule extends UniModule {
    String TAG = "FaceAISDKModule";

    public static int REQUEST_CODE_FOR_FACE_VERIFY = 10086; //去人脸识别
    public static int REQUEST_CODE_FOR_ADD_VERIFY = 10087; //去添加人脸

    private UniJSCallback faceVerifyCallBack,addFaceCallBack;

    /**
     * 成功，识别，取消
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_FOR_FACE_VERIFY && data.hasExtra("respond")) {
            Log.e("TestModule", "原生页面返回----"+data.getStringExtra("respond"));
            if(faceVerifyCallBack != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", "success verify");
                faceVerifyCallBack.invoke(jsonObject);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @UniJSMethod (uiThread = true)
    public void gotoAboutFaceAIPage(){
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            Intent intent = new Intent(mUniSDKInstance.getContext(), NativePageActivity.class);
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE_FOR_FACE_VERIFY);
        }
    }



    //run ui thread
    @UniJSMethod(uiThread = true)
    public void testAsyncFunc(JSONObject options, UniJSCallback callback) {

        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            faceVerifyCallBack=callback;
            Intent intent = new Intent(mUniSDKInstance.getContext(), NativePageActivity.class);
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE_FOR_FACE_VERIFY);
        }

        Log.e(TAG, "testAsyncFunc--"+options);

    }

    //run JS thread
    @UniJSMethod (uiThread = false)
    public JSONObject testSyncFunc(){
        JSONObject data = new JSONObject();
        data.put("code", "success 2222");
        return data;
    }
}
