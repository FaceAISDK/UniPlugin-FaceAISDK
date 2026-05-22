<template>
	<view>
		<button class="gray-button" @tap="faceSearchByCameraDemo">1:N相机人脸搜索识别</button>
		<button class="gray-button" @tap="faceSearchByImageDemo">图片人脸搜索识别(Beta)</button>
	
		<button class="gray-button" @tap="addFaceSearchFeatureByCameraDemo">SDK相机录入人脸信息</button>
		<button class="gray-button" @tap="addFaceSearchFeatureByImageDemo">通过图片录入人脸信息</button>	
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
		faceSearchByCamera,
		faceSearchByImage,
		switchCamera,
		insertFaceSearchFeature,
		insertManyFeatures,
	    addFaceSearchFeatureByCamera,
		addFaceSearchFeatureByImage,
		deleteFaceSearchFeature,
		queryFaceSearchFeature,
		TTSPlayer,
		toastMessage
	} from "@/uni_modules/FaceAI-Search";
	
	// 注意：vue应该使用 testData.uts 而不是 testData.js	
	import { JSON_FACE_FEATURES_DATA } from "./faceFeatureList.js";
	import { base64FaceSearch, base64FaceImage } from './imageData.js';
	
	export default {
		data() {
			return {
				faceID: 'Test',
				faceFeature: 'faceFeature is a string with lenth 1024',
				faceAIResult: 'faceAIResult',
				base64FaceSearch: base64FaceSearch,
				base64FaceImage: base64FaceImage  //建议640*480 人脸图需要遵守规范：https://i.postimg.cc/RCwNy0kV/add-Face.jpg
			}
		},
		onLoad() {

		},
		
		methods: {
			/**
			 * 1:N相机人脸搜索识别，建议使用SDK相机录入人脸，图片没有校验
			 * threshold默认0.85以上，否则可能误识别
			 * */
			faceSearchByCameraDemo: function () {				
				const threshold = 0.85;    // 阈值[0.8.0.9],只有人脸库中匹配到的人脸相似度大于此才有结果返回
				const oneTime = false;     // 搜索页持续存准备人脸搜索， 还是仅仅搜索一次返回结果后关闭搜索页面
				const searchTimeOut = 5;   // 搜索超时时间[3,22],仅仅是oneTime=true才生效，超时没有大于threshold搜索结果自动关闭页面
			                
			    faceSearchByCamera(
			        threshold,
			        oneTime,
			        searchTimeOut,
			        (jsonStr) => { 
			            try {
			                const root = JSON.parse(jsonStr);
			                const results = root.data;
			                const base64 = root.base64; //注意base64可能为空
							console.log("收到搜索结果:", results);

							// 如果需要活体检测，加上相应的判断
							const liveness = root.liveness
			            
			                this.faceAIResult = "【人脸搜索回调】\nList: " + JSON.stringify(results);
			                if (results && results.length > 0) {
								//结果已经排好序，第一个就是相似度最高的
			                    const firstFace = results[0];
			                    const name = firstFace.faceName;
			                    const score = firstFace.faceScore;
			                    
			                    if (name != null ) {
			                            toastMessage(base64,"最匹配:" + name + "," + score);
										TTSPlayer(name)
			                    }
			                } else {
			                    toastMessage("","无结果");
			                }
			            } catch (e) {
			                console.error("解析数据失败:", e);
			            }
			        }
			    );
			},
			
			
			/**
			 * 「图片人脸搜索识别」检测图片中出现的人脸的坐标以及检索是否有大于threshold的最佳匹配人员
			 * 人脸像素宽高需要大于130，无遮挡的清晰正脸
			 */
			faceSearchByImageDemo: function () {
			
			    const threshold = 0.82;    // 阈值[0.8.0.9],只有人脸库中匹配到的人脸相似度大于此才有结果返回			
			    faceSearchByImage(
			        this.base64FaceSearch,
			        threshold,
			        (jsonStr) => {  

			            const root = JSON.parse(jsonStr);  
			            
			            const code = root.code ?? 0;
			            const msg = root.msg ?? "";
			            
			            console.log("图片检索收到响应 -> code:", code, "msg:", msg);
			            console.log("图片检索 -> jsonStr:", jsonStr);
			            this.faceAIResult = jsonStr;  
			
			            if (code == 1) {
			                const results = root.result;  
			                
			                if (results != null && results.length > 0) {
			                    // 1. 统计检测到的人脸总数
			                    const totalFaces = results.length;
			                    
			                    // 2. 统计匹配成功（faceName 不为空）的人数
			                    let matchedCount = 0;
			                    for (let i = 0; i < totalFaces; i++) {
			                        const faceData = results[i];
			                        const faceName = faceData.faceName ?? "";  
			                        
			                        // 如果 faceName 存在且不为空字符串，则认为匹配成功
			                        if (faceName.trim() != "") {
			                            matchedCount++;
			                        }
			                    }
			                    
			                    // 3. 更新 UI 显示结果
			                    this.faceAIResult = `图片检索完成\n` +
			                                        `检测到人脸数: ${totalFaces}\n` +
			                                        `匹配成功人数: ${matchedCount}\n` +
			                                        `详细数据: ${jsonStr}`;
			                                            
			                    toastMessage("", `检测到${totalFaces}人脸，匹配成功${matchedCount}人`);
			
			                } else {
			                    this.faceAIResult = `图片检索完成\n未检测到人脸`;
			                    toastMessage("", "未检测到人脸");
			                }
			            } else {
			                this.faceAIResult = `图片检索失败\n` +
			                                    `错误码(code): ${code}\n` +
			                                    `错误原因(msg): ${msg}`;
			                                        
			                toastMessage("", `检索失败: ${msg}`);
			            }           
			    
			        }   
			    );  
			},
			
			/**
			* 人脸搜索人脸特征录入
			*/
			addFaceSearchFeatureByCameraDemo: function () {  
				addFaceSearchFeatureByCamera(
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
			* 人脸搜索人脸特征录入，通过Base64图片。建议使用SDK相机录入人脸，图片没有校验品质
			* * 建议640*480 人脸图需要遵守规范：https://i.postimg.cc/RCwNy0kV/add-Face.jpg
			*/
			addFaceSearchFeatureByImageDemo: function () {
				addFaceSearchFeatureByImage(
				     this.faceID,
					 this.base64FaceImage,
					 (result)  => {
						//打印结果 json
						console.log("result:", result);
						this.faceAIResult = JSON.stringify(result, ['code', 'msg', 'faceBase64'], 4)
					})
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
			},
			
		   /**
			* 切换前后摄像头，一般0是前置， 1是后置 （但是部分定制Android设备不太标准）
			* 插件目前仅仅支持系统RGB摄像头，UVC协议相机只有原生Android 代码支持
			* */
			switchCameraDemo: function () {
				switchCamera(0)
			},					
			
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