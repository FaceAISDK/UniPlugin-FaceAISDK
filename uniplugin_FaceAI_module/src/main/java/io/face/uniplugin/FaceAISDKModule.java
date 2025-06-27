package io.face.uniplugin;

import static com.ai.face.FaceAIConfig.CACHE_BASE_FACE_DIR;
import static com.ai.face.addFaceImage.AddFaceImageActivity.ADD_FACE_IMAGE_TYPE_KEY;
import static com.ai.face.verify.FaceVerificationActivity.USER_FACE_ID_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.ai.face.FaceAIConfig;
import com.ai.face.FaceAINaviActivity;
import com.ai.face.addFaceImage.AddFaceImageActivity;
import com.ai.face.base.baseImage.FaceAIUtils;
import com.ai.face.utils.VoicePlayer;
import com.ai.face.verify.FaceVerificationActivity;
import com.ai.face.verify.FaceVerifyParams;
import com.ai.face.verify.FaceVerifyWelcomeActivity;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
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
    private String TAG = "FaceAISDKModule";
    private static int REQUEST_CODE_FOR_FACE_VERIFY = 10086; //去人脸识别
    private static int REQUEST_CODE_FOR_ADD_FACE = 10087; //去添加人脸
    private UniJSCallback faceVerifyCallBack,addFaceCallBack;


    public FaceAISDKModule() {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            FaceAIConfig.init(mUniSDKInstance.getContext());
            //语音提示
            VoicePlayer.getInstance().init(mUniSDKInstance.getContext());
        }
    }



    /**
     * 监测onActivityResult 回调数据 ？
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_FOR_FACE_VERIFY ) {
            if(resultCode == Activity.RESULT_OK){
                /**
                 * 0 用户取消
                 * 1 人脸识别成功
                 * 2 活体分数过低
                 * 3 活体检测超时
                 * 4 人脸识别相似度低于阈值
                 */
                if(faceVerifyCallBack != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", data.getIntExtra("code", 0));
                    jsonObject.put("msg", data.getStringExtra("msg"));
                    faceVerifyCallBack.invoke(jsonObject);
                }
            }

        } else if(requestCode == REQUEST_CODE_FOR_ADD_FACE) {
            if(resultCode == Activity.RESULT_OK){
                /**
                 * 0 用户取消
                 * 1 添加人脸成功
                 */
                if(addFaceCallBack != null) {
                    String faceID = data.getStringExtra("faceID");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", data.getIntExtra("code", 0));
                    jsonObject.put("msg", data.getStringExtra("msg"));
                    jsonObject.put("faceID", faceID);

                    String faceFilePath = CACHE_BASE_FACE_DIR + faceID;
                    Bitmap baseBitmap = BitmapFactory.decodeFile(faceFilePath);
                    //对应的Face ID 人脸Bitmap 返回
                    jsonObject.put("faceBase64", Base64BitmapUtil.bitmapToBase64(baseBitmap));
                    addFaceCallBack.invoke(jsonObject);
                }
            }

        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 账号换设备登录的时候需要同步你的账号在服务器上的人脸到SDK
     * 请把人脸图转为Base64编码 Base64.NO_WRAP
     *
     * @param jsonObject
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void insertFace2SDK(JSONObject jsonObject, UniJSCallback callback)  {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            //查询是否存在FaceID
            FaceAIConfig.init(mUniSDKInstance.getContext());

            String faceID=jsonObject.getString("faceID");
            String faceBase64=jsonObject.getString("faceBase64");  //这里接收也改为Base64编码的人脸图
            Bitmap bitmap= Base64BitmapUtil.base64ToBitmap(faceBase64);

            if(bitmap == null) {
                Toast.makeText(mUniSDKInstance.getContext(), "人脸Base64图解析失败",Toast.LENGTH_SHORT).show();
                JSONObject json = new JSONObject();
                json.put("code", 0);
                json.put("msg", "insertFace2SDK 失败");
                json.put("faceID", faceID);
                callback.invoke(json);
                return;
            }

            //其他地方同步过来的人脸可能是不规范的没有经过校准的人脸图（证件照，多人脸，过小等）。disposeBaseFaceImage处理
            FaceAIUtils.Companion.getInstance(((Activity) mUniSDKInstance.getContext()).getApplication())
                    .disposeBaseFaceImage(bitmap, FaceAIConfig.CACHE_BASE_FACE_DIR+faceID, new FaceAIUtils.Callback() {
                        //处理优化人脸成功完成去初始化引擎
                        @Override
                        public void onSuccess(@NonNull Bitmap disposedBitmap) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("code", 1);
                            jsonObject.put("msg", "insertFace2SDK 成功");
                            jsonObject.put("faceID", faceID);
                            callback.invoke(jsonObject);
                        }

                        //底片处理异常的信息回调
                        @Override
                        public void onFailed(@NotNull String msg, int errorCode) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("code", 0);
                            jsonObject.put("msg", msg+"-"+errorCode);
                            jsonObject.put("faceID", faceID);
                            callback.invoke(jsonObject);
                        }
                    });

        }
    }


    /**
     * 是否存在faceID 对应的人脸
     *
     * @param jsonObject
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void isFaceExist(JSONObject jsonObject, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            //查询是否存在FaceID
            FaceAIConfig.init(mUniSDKInstance.getContext());
            String faceID=jsonObject.getString("faceID");
            boolean isExist=FaceAIConfig.isFaceIDExist(faceID);
            callback.invoke(isExist);
        }
    }


    /**
     * 调用SDK 录入人脸
     *
     * @param jsonObject
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void addFaceImage(JSONObject jsonObject, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            addFaceCallBack=callback;

//            if(!checkPermissionOK(mUniSDKInstance.getContext())){
//                Toast.makeText(mUniSDKInstance.getContext(), "请先授权相机权限",Toast.LENGTH_SHORT).show();
//            }

            //人脸图保存路径初始化
            FaceAIConfig.init(mUniSDKInstance.getContext());
            Intent intent=new Intent(mUniSDKInstance.getContext(), AddFaceImageActivity.class)
                    .putExtra(ADD_FACE_IMAGE_TYPE_KEY, AddFaceImageActivity.AddFaceImageTypeEnum.FACE_VERIFY.name());

            intent.putExtra(USER_FACE_ID_KEY,jsonObject.getString("faceID"));
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE_FOR_ADD_FACE);
        }
    }

    /**
     * 人脸识别
     *
     * @param jsonObject
     * @param callback
     */
    @UniJSMethod (uiThread = true)
    public void faceVerify(JSONObject jsonObject, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {

//            if(!checkPermissionOK(mUniSDKInstance.getContext())){
//                Toast.makeText(mUniSDKInstance.getContext(), "请先授权相机权限",Toast.LENGTH_SHORT).show();
//            }

            faceVerifyCallBack=callback;
            FaceAIConfig.init(mUniSDKInstance.getContext());

            //todo 如果没解析成功要处理.参数细节再处理吧
            FaceVerifyParams faceVerifyParams=JSONObject.parseObject(jsonObject.toJSONString(), FaceVerifyParams.class);

            Intent intent=new Intent(mUniSDKInstance.getContext(), FaceVerificationActivity.class);
            intent.putExtra(USER_FACE_ID_KEY,jsonObject.getString("faceID"));
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE_FOR_FACE_VERIFY);
        }
    }


    /**
     * 跳转到关于
     *
     */
    @UniJSMethod (uiThread = true)
    public void gotoAboutFaceAIPage(){
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            FaceAIConfig.init(mUniSDKInstance.getContext());
            Intent enumIntent =new  Intent(mUniSDKInstance.getContext(), FaceAINaviActivity.class);
//            Bundle bundle =new Bundle();
//            bundle.putSerializable(
//                    FaceVerifyWelcomeActivity.FACE_VERIFY_DATA_SOURCE_TYPE,
//                    FaceVerifyWelcomeActivity.DataSourceType.Android_HAL
//            );
//            enumIntent.putExtras(bundle);
            mUniSDKInstance.getContext().startActivity(enumIntent);
        }
    }

    /**
     * 判断是否缺少权限
     */
    public boolean checkPermissionOK(Context mContexts) {
        String permission="Manifest.permission.CAMERA";
        return ContextCompat.checkSelfPermission(mContexts, permission) == PackageManager.PERMISSION_GRANTED;
    }

}
