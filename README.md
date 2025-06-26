## 由于uniApp 插件目前只支持UTS 插件，重构UTS 版本

### UTS重构版本 https://github.com/AnyLifeZLB/uniPlugin_FaceAI_UTS
### UTS重构版本 https://github.com/AnyLifeZLB/uniPlugin_FaceAI_UTS



🪸  



   
   
# FaceAI人脸识别SDK Uni-App 插件

保护伞FaceAI SDK是设备端可离线不联网 人脸识别、动作及近红外IR活体检测、人脸图质量检测
以及人脸搜索（1:N和M:N）SDK，可快速集成实现人脸识别，人脸搜索功能。  
**所有识别都在设备终端本地离线执行，SDK不用联网，不保存不上传任何人脸信息敏感资料更具隐私安全**  

FaceAI SDK主页：https://github.com/AnyLifeZLB/FaceVerificationSDK  
FaceAI App Android 体验包 https://www.pgyer.com/faceVerify （体验全程可开飞行模式✈️）  
 
2023年我们开始封装移动端FaceAI人脸识别SDK，逐步完善后为了Uni-App 开发者更加方便的接入本SDK  
我们封装了UniPlugin-FaceAISDK 插件，插件一期共提供4个方法接口
  - 1  isFaceExist(jsonObject, callback) 检测是否存在faceID 对应的人脸
  - 2  addFaceImage(jsonObject, callback) 调用SDK 录入人脸
  - 3  faceVerify(jsonObject, callback) 人脸识别
  - 4  insertFace2SDK(jsonObject, callback) 同步账号在服务器上的人脸到SDK

插件的使用需要你熟悉Uni-App的开发的基础知识以及少量的原生插件集成经验，相关知识可以参考Uni-App官网
注意 **调试包有相似度分数等提示，正式版没有**

## 插件集成说明
- 如果你是Uni-App 你可以用**HBuilderX4.5.7**打开uniapp示例工程源码/unipluginDemo 自定义基座运行
- 如果你想根据FaceAI SDK 拓展更多功能你可以使用Android Studio **2024.3.1**以上导入本工程完善

IDE版本兼容性问题参考官方，本库开发版本如上；欢迎大家遇到问题提issue，我会及时回复的。
 
## 1. isFaceExist(jsonObject, callback)
检测是否存在faceID 对应的人脸，如果没有你可以先检查你的服务器是否存在，服务器也不存在可以调用SDK
的方法addFaceImage 进行录入人脸

**faceID** 你要检测的人脸ID标识符，在你的业务体系中唯一不可重复，你可以用账户名，身份证号等  

参数 jsonObject 就一个字段 faceID  
参数 callback 会返回检测的结果 true 存在 false 不存在  

## 2. addFaceImage(jsonObject, callback)
调用SDK 录入人脸，SDK 会对人脸录入进行合规检查  
参数 jsonObject 就一个字段 faceID  
参数 callback 会返回添加结果json  
{  
    "code": "0 用户取消/失败,  1:录入完成",  
    "msg":"code 对应的提示",  
    "faceID": "录入人脸对应的face ID",  
    "faceBase64":"人脸图像faceBase64转码"  
}  


## 3. faceVerify(jsonObject, callback)
调用SDK 录入人脸，SDK 会对人脸录入进行合规检查  
**参数 jsonObject**  
{  
   "faceID": "yourFaceID_uniApp", //对应需要识别的人脸ID  
   "threshold": 0.85, //人脸识别相似度阈值  
   "silentThreshold": 0.85,//活体分数通过阈值  
   "faceLivenessType": 0, //活体检测类型 //0 SILENT_MOTION; 1 MOTION; 2 SILENT; 3 NONE;  
   "motionStepSize": 1, //动作活体检测动作数量。比如2 表示需要用户做2个动作才能通过活体检测  
   "verifyTimeOut": 16 //活体检测超时时间 秒  
}  


**参数 callback 会返回添加识别json**   
{  
   "code": "0 用户取消/失败,  1:录入完成",  
   "msg":"code 对应的提示",  
   "faceID": "录入人脸对应的face ID",  
   "faceBase64":"人脸图像faceBase64转码"  
}  


## 4. insertFace2SDK(jsonObject, callback) 
同步账号在服务器上的人脸到SDK，比如你在A设备登录的时候已经录入人脸并且同步到你的服务器了    
那么登录B设备后不用调用SDK 再次录入人脸，你仅仅需要把人脸数据同步到SDK 就可以  




