package com.ai.face.verify;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.ai.face.faceVerify.verify.liveness.LivenessType;


/**
 *
 *
 */
public class FaceVerifyParams implements Parcelable {
    private String faceID;
    private float threshold=0.85f;
    private float silentThreshold=0.85f;
    private LivenessType livenessType=LivenessType.SILENT_MOTION;
    //0 SILENT_MOTION; 1 MOTION; 2 SILENT; 3 NONE;
    private int  faceLivenessType=0;

    private int motionStepSize=1;
    private int verifyTimeOut=16;

    //OPEN_MOUSE = 1;  SMILE = 2;  BLINK = 3; SHAKE_HEAD = 4; NOD_HEAD = 5;
    private int exceptMotionLivelessType; //不想要的动作活体种类


    public FaceVerifyParams() {

    }

    protected FaceVerifyParams(Parcel in) {
        faceID = in.readString();
        threshold = in.readFloat();
        silentThreshold = in.readFloat();
        faceLivenessType = in.readInt();
        motionStepSize = in.readInt();
        verifyTimeOut = in.readInt();
        exceptMotionLivelessType = in.readInt();
    }



    public String getFaceID() {
        return faceID;
    }

    public void setFaceID(String faceID) {
        this.faceID = faceID;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public float getSilentThreshold() {
        return silentThreshold;
    }

    public void setSilentThreshold(float silentThreshold) {
        this.silentThreshold = silentThreshold;
    }

    public LivenessType getLivenessType() {
        return livenessType;
    }

    public void setLivenessType(LivenessType livenessType) {
        this.livenessType = livenessType;
    }

    public int getFaceLivenessType() {
        return faceLivenessType;
    }

    public void setFaceLivenessType(int faceLivenessType) {
        this.faceLivenessType = faceLivenessType;

        //0 SILENT_MOTION; 1 MOTION; 2 SILENT; 3 NONE;
        switch (faceLivenessType){
            case 0:
                livenessType=LivenessType.SILENT_MOTION;
                break;
            case 1:
                livenessType=LivenessType.MOTION;
                break;
            case 2:
                livenessType=LivenessType.SILENT;
                break;
            case 3:
                livenessType=LivenessType.NONE;
                break;
        }
    }

    public int getMotionStepSize() {
        return motionStepSize;
    }

    public void setMotionStepSize(int motionStepSize) {
        this.motionStepSize = motionStepSize;
    }

    public int getVerifyTimeOut() {
        return verifyTimeOut;
    }

    public void setVerifyTimeOut(int verifyTimeOut) {
        this.verifyTimeOut = verifyTimeOut;
    }

    public int getExceptMotionLivelessType() {
        return exceptMotionLivelessType;
    }

    public void setExceptMotionLivelessType(int exceptMotionLivelessType) {
        this.exceptMotionLivelessType = exceptMotionLivelessType;
    }


    public static final Creator<FaceVerifyParams> CREATOR = new Creator<>() {
        @Override
        public FaceVerifyParams createFromParcel(Parcel in) {
            return new FaceVerifyParams(in);
        }

        @Override
        public FaceVerifyParams[] newArray(int size) {
            return new FaceVerifyParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(faceID);
        dest.writeFloat(threshold);
        dest.writeFloat(silentThreshold);
        dest.writeInt(faceLivenessType);
        dest.writeInt(motionStepSize);
    }



}
