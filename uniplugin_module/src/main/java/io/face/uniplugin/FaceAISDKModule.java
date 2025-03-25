package io.face.uniplugin;

import static com.ai.face.addFaceImage.AddFaceImageActivity.ADD_FACE_IMAGE_TYPE_KEY;
import static com.ai.face.verify.FaceVerificationActivity.USER_FACE_ID_KEY;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ai.face.AboutFaceAppActivity;
import com.ai.face.FaceAIConfig;
import com.ai.face.addFaceImage.AddFaceImageActivity;
import com.ai.face.base.baseImage.FaceAIUtils;
import com.ai.face.verify.FaceVerificationActivity;
import com.ai.face.verify.FaceVerifyParams;
import com.ai.face.verify.FaceVerifyWelcomeActivity;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

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
    private static int REQUEST_CODE_FOR_ADD_VERIFY = 10087; //去添加人脸
    private UniJSCallback faceVerifyCallBack,addFaceCallBack;


    public FaceAISDKModule() {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            FaceAIConfig.init(mUniSDKInstance.getContext());
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

        } else if(requestCode == REQUEST_CODE_FOR_ADD_VERIFY ) {
            if(resultCode == Activity.RESULT_OK){
                /**
                 * 0 用户取消
                 * 1 添加人脸成功
                 */
                if(addFaceCallBack != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", data.getIntExtra("code", 0));
                    jsonObject.put("msg", data.getStringExtra("msg"));
                    addFaceCallBack.invoke(jsonObject);
                }
            }

        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 插入一张远程URL 的人脸图到SDK，待完善
     *
     * @param options
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void insertFace(JSONObject options, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            //查询是否存在FaceID
            FaceAIConfig.init(mUniSDKInstance.getContext());
            String faceID=options.getString("faceID");
            String faceURL=options.getString("faceURL");
            //加载网络人脸图转为Bitmap 然后保存到SDK
            Glide.with(mUniSDKInstance.getContext())
                    .asBitmap()
                    .load("https://img2023.cnblogs.com/blog/2894189/202303/2894189-20230320143847971-1125640597.png")
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //其他地方同步过来的人脸可能是不规范的没有经过校准的人脸图（证件照，多人脸，过小等）。disposeBaseFaceImage处理
                            FaceAIUtils.Companion.getInstance(((Activity) mUniSDKInstance.getContext()).getApplication())
                                    .disposeBaseFaceImage(resource, FaceAIConfig.CACHE_BASE_FACE_DIR+faceID, new FaceAIUtils.Callback() {
                                        //处理优化人脸成功完成去初始化引擎
                                        @Override
                                        public void onSuccess(@NonNull Bitmap disposedBitmap) {
                                        }

                                        //底片处理异常的信息回调
                                        @Override
                                        public void onFailed(@NotNull String msg, int errorCode) {

                                        }
                                    });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }
                    });

        }
    }


    /**
     * 是否存在faceID 对应的人脸
     *
     * @param options
     * @param callback
     */
    @UniJSMethod(uiThread = true)
    public void isFaceExist(JSONObject options, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            //查询是否存在FaceID
            FaceAIConfig.init(mUniSDKInstance.getContext());
            String faceID=options.getString("faceID");
            boolean isExist=FaceAIConfig.isFaceIDExist(faceID);
            callback.invoke(isExist);
        }
    }



    //run ui thread
    @UniJSMethod(uiThread = true)
    public void addFaceImage(JSONObject options, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            addFaceCallBack=callback;
            //人脸图保存路径初始化
            FaceAIConfig.init(mUniSDKInstance.getContext());
            Intent intent=new Intent(mUniSDKInstance.getContext(), AddFaceImageActivity.class)
                    .putExtra(ADD_FACE_IMAGE_TYPE_KEY, AddFaceImageActivity.AddFaceImageTypeEnum.FACE_VERIFY.name());

            intent.putExtra(USER_FACE_ID_KEY,options.getString("faceID"));
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE_FOR_ADD_VERIFY);
        }
    }

    //run JS thread
    @UniJSMethod (uiThread = true)
    public void faceVerify(JSONObject options, UniJSCallback callback) {
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            faceVerifyCallBack=callback;
            FaceAIConfig.init(mUniSDKInstance.getContext());

            //todo 如果没解析成功要处理
            FaceVerifyParams faceVerifyParams=JSONObject.parseObject(options.toJSONString(), FaceVerifyParams.class);

            Intent intent=new Intent(mUniSDKInstance.getContext(), FaceVerificationActivity.class);
            intent.putExtra(USER_FACE_ID_KEY, options.getString("faceImageURL"));
            intent.putExtra(USER_FACE_ID_KEY,options.getString("faceID"));
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

            Intent enumIntent =new  Intent(mUniSDKInstance.getContext(), FaceVerifyWelcomeActivity.class);
            Bundle bundle =new Bundle();
            bundle.putSerializable(
                    FaceVerifyWelcomeActivity.FACE_VERIFY_DATA_SOURCE_TYPE,
                    FaceVerifyWelcomeActivity.DataSourceType.Android_HAL
            );
            enumIntent.putExtras(bundle);
            mUniSDKInstance.getContext().startActivity(enumIntent);

//            Intent intent = new Intent(mUniSDKInstance.getContext(), FaceVerifyWelcomeActivity.class);
//            mUniSDKInstance.getContext().startActivity(intent);

        }
    }

}
