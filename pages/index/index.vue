<template>
  <view class="container">
    <button class="gray-button" @tap="startFaceSearchDemo">1:N人脸搜索</button>
    <button class="gray-button" @tap="addFaceSearchFeatureDemo">1:N人脸搜索录入人脸</button>
    <button class="gray-button" @tap="deleteFaceSearchFeatureDemo">删除人脸搜索特征值</button>
    <button class="gray-button" @tap="queryFaceSearchFeatureDemo">查询人脸搜索特征值</button>
    <button class="gray-button" @tap="insertFaceSearchFeatureDemo">同步人脸搜索特征值</button>
    <button class="gray-button" @tap="insertManyFaceFeatureSDemo">批量同步人脸搜索特征值</button>

    <view class="result-box">
      <view class="email-text">Email: FaceAISDK.Service@gmail.com</view>
      <scroll-view scroll-y="true" class="scroll-view-box">
        <text class="text-content">{{ faceAIResult }}</text>
      </scroll-view>
    </view>
  </view>
</template>

<script lang="uts">
  // 引入 UTS 插件接口
  import {
    startFaceSearch,
    insertFaceSearchFeature,
    insertManyFeatures,
    addFaceSearchFeature,
    deleteFaceSearchFeature,
    queryFaceSearchFeature,
    ResultJSON
  } from "@/uni_modules/FaceAI-Search";

  // 引入测试数据
  import { JSON_FACE_FEATURES_DATA } from "./testData.uts";

  export default {
    data() {
      return {
        // 定义数据并初始化
        faceID: 'Test',
        faceFeature: 'faceFeature is a string with lenth 1024',
        faceAIResult: '等待操作结果...'
      }
    },
    methods: {
      /**
       * 1:N 人脸搜索
       */
      startFaceSearchDemo() {
        const threshold = 0.88; // 阈值
        const oneTime = false;  // 是否只搜索一次
		const highRes = false;  // true，高分辨率模式，远距离识别更佳，但会牺牲性能和速度以及定制设备不兼容黑屏
        const camId = 0;   // 0:前置, 1:后置

        startFaceSearch(
          threshold,
          oneTime,
          highRes,
          camId,
          (jsonStr: string) => {
            // 这里会持续收到回调
            console.log("收到搜索结果:", jsonStr);
            this.faceAIResult = "【人脸搜索回调】\n" + jsonStr;
          }
        );
      },

      /**
       * 录入人脸特征（通过相机）
       */
      addFaceSearchFeatureDemo() {
        addFaceSearchFeature(
          this.faceID,
          1, // 1.快速模式 2.精确模式
          true, // 显示确认框
          (result: ResultJSON) => {
            // 建议直接 stringify 对象，避免兼容性问题
            this.faceAIResult = "【录入结果】\n" + JSON.stringify(result);
            console.log(this.faceAIResult);
          }
        )
      },

      /**
       * 删除人脸特征
       */
      deleteFaceSearchFeatureDemo() {
        deleteFaceSearchFeature(
          this.faceID,
          (result: ResultJSON) => {
            this.faceAIResult = "【删除结果】\n" + JSON.stringify(result);
          }
        )
      },

      /**
       * 查询人脸特征
       * faceID不传值（""）代表查询所有
       */
      queryFaceSearchFeatureDemo() {
        queryFaceSearchFeature(
          "", // 空字符串查询所有
          (result: string) => {
            // result 已经是 JSON 字符串，直接显示
            this.faceAIResult = "【查询结果】\n" + result;
            console.log("查询回调:", result);
          }
        )
      },

      /**
       * 同步单个人脸特征
       */
      insertFaceSearchFeatureDemo() {
        insertFaceSearchFeature(
          this.faceID,
          this.faceFeature,
          "tag",
          "group",
          (result: ResultJSON) => {
            this.faceAIResult = "【同步结果】\n" + JSON.stringify(result);
          }
        )
      },

      /**
       * 批量同步人脸特征
       */
      insertManyFaceFeatureSDemo() {
        insertManyFeatures(
          JSON_FACE_FEATURES_DATA,
          (result: ResultJSON) => {
            this.faceAIResult = "【批量同步结果】\n" + JSON.stringify(result);
          }
        )
      }
    }
  }
</script>

<style>
  .container {
    padding: 20rpx;
  }

  .gray-button {
    background-color: #ffffff;
    color: #800080;
    border: 1px solid #eeeeee;
    margin-bottom: 20rpx;
    font-size: 30rpx;
  }

  .result-box {
    margin-top: 30rpx;
    padding: 10rpx;
  }

  .email-text {
    font-size: 24rpx;
    color: #999;
    margin-bottom: 10rpx;
  }

  .scroll-view-box {
    height: 500rpx; /* 增加高度以便更好地查看大量 JSON 数据 */
    border: 1px solid #ccc;
    border-radius: 10rpx;
    background-color: #f8f8f8;
    padding: 15rpx;
  }

  .text-content {
    font-size: 26rpx;
    color: #333;
    /* 关键：确保 JSON 字符串在狭窄屏幕上能换行 */
    word-break: break-all;
    white-space: pre-wrap;
  }
</style>