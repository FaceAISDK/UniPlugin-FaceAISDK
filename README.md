## 插件简介

人脸搜索识别Android端侧可离线UTS API插件，支持uniappX和uniapp。    
不需后端部署完全可离线运行，不收集用户隐私数据更加安全便捷支持Android5-16  

**感谢大家收藏与点赞**，如有问题可描述你的使用场景和问题提issues到：https://github.com/FaceAISDK/FaceSearch_uniapp_plugin/issues
或发送邮件到 FaceAISDK.Service@gmial.com  
**反馈问题请说明使用场景，Vue2/Vue3/uvue,哪个功能，什么设备什么场景尽可能提供详细信息**

快速体验完整人脸识别功能可以下载Demo App：
<div align=center>
<img src="https://www.pgyer.com/app/qrcode/hiface" width = 19%   alt="扫一扫下载Demo"/>
</div>


## 使用方法
  如果你是第一次运行UTS插件工程/引入UTS API插件，你应先安装官方说明配置好基础环境 [基础环境](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html) 

  **开发调试阶段请开启调试模式**，Android-manifest.json distribute配置 "debuggable" : true,

  ### 1.下载Demo工程[](https://github.com/FaceAISDK/FaceSearch_uniapp_plugin)先跑通。熟悉后参考文档集成到主项目

  ### 2.按照文档 -》把插件引入项目（即 import {需要的方法} from "@/uni_modules/FaceAI-Search";）

  ### 3.运行 -》运行到手机或模拟器 -》**制作自定义调试基座** -》打包 等基座制作完成
   . 
   ![制作自定义调试基座](https://i.postimg.cc/QVZFgycd/1.png)

  ### 4.运行 -》运行到iOS/Android基座-》**使用自定义基座运行**-》选择手机-》运行
   . 
   ![运行到手机](https://i.postimg.cc/QdwtZM60/2.png)
	
  **按照步骤一定要先制作自定义调试基准，然后运行的时候使用自定义基准。偶尔打包服务器失败请重试**  
	
	
  本插件为人脸搜索（1:N）如果 你需要1:1人脸验证（支持iOS，Android）请移步：https://ext.dcloud.net.cn/plugin?id=23881
  目前人脸搜索识别UTS插件还没有原生Android那么完善，更多功能和更好体验请参考原生Android实现https://github.com/FaceAISDK/FaceAISDK_Android

  Powered by FaceAISDK Copyright©2026. **高精度版本联系 FaceAISDK.Service@gmail.com**

## 人脸抓拍能力

当前工程在原有人脸搜索 UTS API 基础上新增了三种抓拍入口：

- `captureFaceByCamera`：全屏 UTS API，兼容 uni-app Vue 与 uni-app x uvue。
- `<face-ai-capture>`：使用 `native-view` 的标准模式组件，面向 uni-app x。
- `<face-ai-capture-compat>`：兼容模式组件，面向 app-nvue 和 uni-app x VDOM。

三种入口都会返回 `croppedBase64`、`silentScore`、`originBase64`。全屏 UTS API 保持连续回调；两种嵌入组件每次成功后会暂停，只有业务端显式调用 `retry()` 才会进入下一轮。完整参数和示例见 `uni_modules/FaceAI-Search/readme.md`。

抓拍推荐使用 `rotationDegrees=-1` 自动跟随当前显示方向，两个组件也以此为默认值；Demo 已开启横竖屏自动旋转，并为手机、Android 平板和可调整大小窗口提供自适应抓拍布局。两个组件验收页均提供“开始/再次抓拍”和“切换前后相机”按钮。
