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
	// 引入模块，注意保持路径一致
	import {
		startFaceSearch,
		insertFaceSearchFeature,
		insertManyFeatures,
		addFaceSearchFeature,
		deleteFaceSearchFeature,
		queryFaceSearchFeature,
		toastMessage
	} from "@/uni_modules/FaceAI-Search";
	
	// 注意：vue应该使用 testData.uts 而不是 testData.js	
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
			startFaceSearchDemo: function () {
			    const threshold = 0.88;
			    const oneTime = false;
			    const searchTimeOut = 5;
			    const highRes = false;
			    const camId = 0;
			                
			    startFaceSearch(
			        threshold,
			        oneTime,
			        searchTimeOut,
			        highRes,
			        camId,
			        (jsonStr) => { 
			          
			            try {
			                // Vue2 测试通过
			                const root = JSON.parse(jsonStr);
			                const results = root.data;
			                const base64 = root.base64;
							console.log("收到搜索结果:", results);
							console.log("base64:", base64);
			            
			                this.faceAIResult = "【人脸搜索回调】\nList: " + JSON.stringify(results);
			                if (results && results.length > 0) {
								//结果已经排好序，第一个就是相似度最高的
			                    const firstFace = results[0];
			                    const name = firstFace.faceName;
			                    const score = firstFace.faceScore;
			                    
			                    if (name != null) {
			                        toastMessage("最匹配:" + name + "," + score);
			                    }
			                } else {
			                    toastMessage("无结果");
			                }
			            } catch (e) {
			                console.error("解析数据失败:", e);
			            }
			        }
			    );
			},
			
			/**
			* 人脸搜索人脸特征录入
			*/
			addFaceSearchFeatureDemo: function () {
				addFaceSearchFeature(
					this.faceID,
					1,    // 1.快速模式 2.精确模式
					true, // 是否显示确认框
					(result) => { 
						// 打印结果 json
						this.faceAIResult = JSON.stringify(result, ['code', 'msg', 'faceBase64'], 4)
					}
				)
			},
			
			/**
			* 删除人脸搜索人脸特征
			*/
			deleteFaceSearchFeatureDemo: function () {
				deleteFaceSearchFeature(
					this.faceID,
					(result) => {
						this.faceAIResult = JSON.stringify(result)
					}
				)
			},
			
			/**
			* 查询人脸搜索人脸特征
			*/
			queryFaceSearchFeatureDemo: function () {
				queryFaceSearchFeature(
					this.faceID,  // 不传则查询本地所有的数据
					(result) => { // 移除 :string 类型
						this.faceAIResult = "【人脸查询回调】\n" + result;
					}
				)
			},
			
			
		   /**
			* 人脸搜索人脸特征更新同步
			*/
			insertFaceSearchFeatureDemo: function () {
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
			* 批量操作人脸搜索人脸特征更新同步
			*/
			insertManyFaceFeatureSDemo: function () {
				insertManyFeatures(
					JSON_FACE_FEATURES_DATA,
					(result) => {
						this.faceAIResult = JSON.stringify(result)
					}
				)
			}
	   }			
	}
</script>

<style>
    /* 给滚动区域一个固定高度和边框 */
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
</style>

<style>
    .gray-button {
    	background-color: #ffffff;
    	color: #800080;
    	border: none;
    }
</style>