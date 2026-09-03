package uts.sdk.modules.uniFaceAISDK;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ai.face.base.addFace.CaptureFaceDispose;
import com.faceAI.demo.R;
import com.faceAI.demo.base.AbsBaseActivity;

/**
 * UTS API 使用的全屏持续人脸抓拍页面。
 * 具体相机和抓拍能力由 CaptureFaceNativeView 提供，保证与两种组件入口行为一致。
 */
public class CaptureFaceActivity extends AbsBaseActivity {
    public static final String PERFORMANCE_MODE = "CAPTURE_FACE_PERFORMANCE_MODE";
    public static final String NEED_LIVENESS_CHECK = "CAPTURE_FACE_NEED_LIVENESS_CHECK";
    public static final String CAMERA_ID = "CAPTURE_FACE_CAMERA_ID";
    public static final String LINEAR_ZOOM = "CAPTURE_FACE_LINEAR_ZOOM";
    public static final String ROTATION_DEGREES = "CAPTURE_FACE_ROTATION_DEGREES";
    public static final String CAMERA_SIZE_HIGH = "CAPTURE_FACE_CAMERA_SIZE_HIGH";

    private CaptureFaceNativeView captureFaceView;
    private int performanceMode = CaptureFaceDispose.PERFORMANCE_MODE_FAST;
    private boolean needLivenessCheck = true;
    private int cameraId = 0;
    private float linearZoom = 0.12f;
    private int rotationDegrees = 0;
    private boolean cameraSizeHigh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        readOptions(getIntent());

        FrameLayout root = new FrameLayout(this);
        captureFaceView = new CaptureFaceNativeView(this);
        captureFaceView.setResultCallback((croppedBase64, silentScore, originBase64) -> {
            CaptureFaceResultManager.INSTANCE.sendResult(
                    croppedBase64,
                    silentScore,
                    originBase64
            );
            return kotlin.Unit.INSTANCE;
        });
        captureFaceView.setErrorCallback((code, message) -> {
            CaptureFaceResultManager.INSTANCE.sendError(code, message);
            return kotlin.Unit.INSTANCE;
        });
        captureFaceView.setCameraChangedCallback((newCameraId) -> {
            cameraId = newCameraId;
            return kotlin.Unit.INSTANCE;
        });
        root.addView(
                captureFaceView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        ImageView closeButton = new ImageView(this);
        closeButton.setImageResource(R.drawable.ic_arrow_back_24);
        closeButton.setPadding(dp(9), dp(9), dp(9), dp(9));
        closeButton.setContentDescription("Close capture face");
        closeButton.setOnClickListener((View view) -> finish());
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(47), dp(47));
        closeParams.gravity = Gravity.START | Gravity.TOP;
        closeParams.setMargins(dp(15), dp(15), 0, 0);
        root.addView(closeButton, closeParams);

        ImageView switchButton = new ImageView(this);
        switchButton.setImageResource(R.drawable.switch_camera);
        switchButton.setBackgroundResource(R.drawable.bg_switch_camera_button);
        switchButton.setPadding(dp(7), dp(7), dp(7), dp(7));
        switchButton.setContentDescription(getString(R.string.switch_camera));
        switchButton.setOnClickListener((View view) -> captureFaceView.toggleCamera());
        FrameLayout.LayoutParams switchParams = new FrameLayout.LayoutParams(dp(34), dp(34));
        switchParams.gravity = Gravity.END | Gravity.TOP;
        switchParams.setMargins(0, dp(22), dp(15), 0);
        root.addView(switchButton, switchParams);

        setContentView(root);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (captureFaceView != null) {
            captureFaceView.start(
                    performanceMode,
                    needLivenessCheck,
                    cameraId,
                    linearZoom,
                    rotationDegrees,
                    cameraSizeHigh
            );
        }
    }

    @Override
    protected void onStop() {
        if (captureFaceView != null) {
            captureFaceView.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (captureFaceView != null) {
            captureFaceView.release();
            captureFaceView = null;
        }
        CaptureFaceResultManager.INSTANCE.clear();
        super.onDestroy();
    }

    private void readOptions(Intent intent) {
        if (intent == null) return;
        performanceMode = intent.getIntExtra(PERFORMANCE_MODE, performanceMode);
        needLivenessCheck = intent.getBooleanExtra(NEED_LIVENESS_CHECK, needLivenessCheck);
        cameraId = intent.getIntExtra(CAMERA_ID, cameraId);
        linearZoom = intent.getFloatExtra(LINEAR_ZOOM, linearZoom);
        rotationDegrees = intent.getIntExtra(ROTATION_DEGREES, rotationDegrees);
        cameraSizeHigh = intent.getBooleanExtra(CAMERA_SIZE_HIGH, cameraSizeHigh);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
