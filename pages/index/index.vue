<template>
  <view>
    <button class="gray-button" @tap="startFaceSearchDemo">1:N人脸搜索</button>
    <button class="gray-button" @tap="addFaceSearchFeatureDemo">1:N人脸搜索录入人脸</button>
    <button class="gray-button" @tap="deleteFaceSearchFeatureDemo">删除人脸搜索特征值</button>
    <button class="gray-button" @tap="queryFaceSearchFeatureDemo">查询人脸搜索特征值</button>
    <button class="gray-button" @tap="insertFaceSearchFeatureDemo">同步人脸搜索特征值</button>
    <button class="gray-button" @tap="insertManyFaceFeatureSDemo">批量同步人脸搜索特征值</button>

    <view class="result-box">
      <view> Email: FaceAISDK.Service@gmail.com</view>
      <scroll-view scroll-y="true" class="scroll-view-box">
        <text class="text-content">{{faceAIResult}}</text>
      </scroll-view>
    </view>
  </view>
</template>

<script>
// 1. 引入模块 (去除 .uts 后缀或改为 .js)
import {
  startFaceSearch,
  insertFaceSearchFeature,
  insertManyFeatures,
  addFaceSearchFeature,
  deleteFaceSearchFeature,
  queryFaceSearchFeature,
  toastMessage
} from "@/uni_modules/FaceAI-Search";

import { JSON_FACE_FEATURES_DATA } from "./testData.js";

export default {
  data() {
    return {
      faceID: 'Test',
      faceFeature: 'faceFeature is a string with lenth 1024',
      faceAIResult: 'faceAIResult'
    }
  },
  onLoad() {

  },

  methods: {
    /**
     * 人脸搜索识别
     */
    startFaceSearchDemo: function() {
      const threshold = 0.88; // 阈值
      const oneTime = false; // 持续搜索
      const highRes = false; // 高分辨率模式
      const camId = 0; // 前置摄像头

      startFaceSearch(
        threshold,
        oneTime,
        highRes,
        camId,
        (jsonStr) => {
          console.log("收到搜索结果:", jsonStr);
          this.faceAIResult = "【人脸搜索回调】\n" + jsonStr;

          try {
            // JS中 JSON.parse 直接得到对象/数组
            const results = JSON.parse(jsonStr);
            if (results && results.length > 0) {
              const firstFace = results[0];
              const name = firstFace.faceName; // 直接访问属性
              if (name) {
                toastMessage("识别成功: " + name);
              }
            }
          } catch (e) {
            console.error("解析数据失败:", e);
          }
        }
      );
    },

    /**
     * 人脸特征录入
     */
    addFaceSearchFeatureDemo: function() {
      addFaceSearchFeature(
        this.faceID,
        1, // 1.快速模式 2.精确模式
        true, // 是否显示确认框
        (result) => {
          this.faceAIResult = JSON.stringify(result, ['code', 'msg', 'faceBase64'], 4)
        }
      )
    },

    /**
     * 删除特征值
     */
    deleteFaceSearchFeatureDemo: function() {
      deleteFaceSearchFeature(
        this.faceID,
        (result) => {
          this.faceAIResult = JSON.stringify(result)
        }
      )
    },

    /**
     * 查询特征值
     */
    queryFaceSearchFeatureDemo: function() {
      queryFaceSearchFeature(
        this.faceID,
        (result) => {
          this.faceAIResult = "【人脸查询回调】\n" + result;
        }
      )
    },

    /**
     * 同步特征值
     */
    insertFaceSearchFeatureDemo: function() {
      insertFaceSearchFeature(
        this.faceID,
        this.faceFeature,
        "tag",
        "group",
        (result) => {
          this.faceAIResult = JSON.stringify(result)
        })
    },

    /**
     * 批量同步
     */
    insertManyFaceFeatureSDemo: function() {
      insertManyFeatures(
        JSON_FACE_FEATURES_DATA,
        (result) => {
          this.faceAIResult = JSON.stringify(result)
        })
    }
  }
}
</script>

<style>
  .result-box {
    margin: 20rpx;
  }

  .scroll-view-box {
    height: 400rpx;
    border: 1px solid #ccc;
    border-radius: 10rpx;
    background-color: #f8f8f8;
    padding: 15rpx;
    box-sizing: border-box;
  }

  .text-content {
    font-size: 28rpx;
    color: #333;
    white-space: pre-wrap;
  }

  .gray-button {
    background-color: #ffffff;
    color: #800080;
    border: none;
  }
</style>