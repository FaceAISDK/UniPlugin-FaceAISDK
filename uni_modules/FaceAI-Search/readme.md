## 插件简介

人脸搜索识别Android端侧可离线UTS API插件，支持uniappX和uniapp，支持图片搜索；不需后端部署完全可离线运行   
插件不适合金融支付以及14周岁以下群体（没有针对模型优化训练）,摄像头需成像清晰，宽动态值大于105DB(室外120DB) 

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
	
  Powered by FaceAISDK Copyright©2026. **高精度版本联系 FaceAISDK.Service@gmail.com**  
	
	
  本插件为人脸搜索（1:N）如果 你需要1:1人脸验证（支持iOS，Android）请移步：https://ext.dcloud.net.cn/plugin?id=23881  
  目前人脸搜索识别UTS插件还没有原生Android那么完善，更多功能和更好体验请参考原生Android实现https://github.com/FaceAISDK/FaceAISDK_Android  
	

## 持续人脸抓拍

持续抓拍能力对应 Android `AddFaceCallBack.onCompleted(cropped, silentScore, origin)`，每次检测到符合角度和尺寸要求的人脸后返回：

- `croppedBase64`：SDK 矫正、裁剪后的 224×224 人脸 JPEG Data URL。
- `silentScore`：静默活体分数；仅在 `needLivenessCheck=true` 时有效。
- `originBase64`：本次检测对应的相机原图 JPEG Data URL。

标准模式和兼容模式组件在每次成功后会暂停检测，不会自动续拍；只有业务端显式调用 `retry()` 才会进入下一轮。全屏 UTS API 为保持原有连续抓拍语义，会由全屏 Activity 在每次结果回调后调用 `retry()`。内部只允许一个结果进行编码，避免相机帧堆积导致内存上涨。

### UTS API：全屏持续抓拍

支持 uni-app 的 Vue 页面和 uni-app x 的 uvue 页面：

```ts
import {
  captureFaceByCamera,
  CaptureFaceResult,
  CaptureFaceError
} from "@/uni_modules/FaceAI-Search"

captureFaceByCamera(
  1,      // performanceMode: -1/0/1/2
  true,   // needLivenessCheck
  0,      // cameraId: 0 前置、1 后置
  0.12,   // linearZoom: 0~1
  -1,     // rotationDegrees: -1 自动跟随屏幕，或固定为 0/90/180/270
  false,  // cameraSizeHigh
  (result: CaptureFaceResult) => {
    console.log(result.croppedBase64)
    console.log(result.silentScore)
    console.log(result.originBase64)
  },
  (error: CaptureFaceError) => {
    console.error(error.code, error.message)
  }
)
```

调用后打开全屏 `CaptureFaceActivity`，点击左上角返回按钮结束，点击右上角按钮切换前/后摄像头。

### 标准模式组件：uni-app x

HBuilderX 4.31+ 可在 uvue 页面直接使用 easycom 组件：

```vue
<template>
  <view style="flex: 1;">
    <face-ai-capture
      ref="captureRef"
      style="width: 100%; flex: 1;"
      :performance-mode="1"
      :need-liveness-check="true"
      :camera-id="0"
      :rotation-degrees="-1"
      :show-face-cover="false"
      :auto-start="false"
      @result="onResult"
      @tips="onTips"
      @error="onError"
    />
    <button @tap="captureOnce">{{ started ? '再次抓拍' : '开始抓拍' }}</button>
    <button @tap="toggleCamera">切换前后相机</button>
  </view>
</template>

<script setup lang="uts">
import { CaptureFaceResult } from "@/uni_modules/FaceAI-Search"

const captureRef = ref<ComponentPublicInstance | null>(null)
const started = ref(false)

function onResult(result: CaptureFaceResult) {
  console.log(result.silentScore)
}

function captureOnce() {
  captureRef.value?.$callMethod(started.value ? "retry" : "start")
  started.value = true
}

function toggleCamera() {
  captureRef.value?.$callMethod("toggleCamera")
}
</script>
```

组件挂载前应确保相机权限已经授予，并确保父容器具有可用高度。标准模式和兼容模式组件提供相同的参数、方法和事件。

#### 组件 Props

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `performanceMode` | `number` | `1` | 抓拍性能模式：`-1` 无限制、`0` 简单、`1` 快速、`2` 精确 |
| `needLivenessCheck` | `boolean` | `true` | 是否计算静默活体分数 |
| `cameraId` | `number` | `0` | 初始镜头：`0` 前置、`1` 后置；初始化时目标不存在会降级到可用镜头 |
| `linearZoom` | `number` | `0.12` | 线性变焦，范围 `0~1` |
| `rotationDegrees` | `number` | `-1` | `-1` 自动跟随当前显示方向；也可固定为 `0/90/180/270` |
| `cameraSizeHigh` | `boolean` | `false` | 是否使用 1280×720 高分辨率分析 |
| `showFaceCover` | `boolean` | `false` | 是否显示 Android 原生 `FaceCoverView`（圆形遮罩和原生提示）；关闭不影响 `tips` 事件 |
| `autoStart` | `boolean` | `true` | 原生 View 就绪后是否自动调用 `start()` |

#### 组件方法

| 方法 | 返回值 | 说明 |
| --- | --- | --- |
| `start()` | `void` | 使用当前 Props 开始抓拍；重复调用会重启采集会话 |
| `stop()` | `void` | 停止相机和抓拍会话 |
| `retry()` | `void` | 每次成功后手动允许 SDK 进入下一轮；成功回调后组件不会自动调用 |
| `switchCamera(cameraId)` | `void` | 运行期间切换到指定镜头，`0` 前置、`1` 后置 |
| `toggleCamera()` | `void` | 运行期间在当前实际镜头与另一颗前/后镜头之间切换 |
| `canSwitchCamera()` | `boolean` | CameraProvider 就绪且另一颗前/后镜头存在时返回 `true` |

切换时只重绑 CameraX 的预览和分析用例，抓拍引擎保持运行。目标镜头不存在时保留当前预览并触发 `error`；绑定失败时会尝试恢复原镜头。

`retry()` 只在上一次抓拍已经完成时生效；在正在检测时重复调用不会并发开启多轮抓拍。

#### 组件事件与返回字段

| 事件 | 字段 | 类型 | 说明 |
| --- | --- | --- | --- |
| `ready` | 无 | - | 原生 View 已创建；不代表 CameraProvider 已完成初始化 |
| `result` | `croppedBase64` | `string` | SDK 矫正、裁剪后的 224×224 人脸 JPEG Data URL |
| `result` | `silentScore` | `number` | 静默活体分数；仅在 `needLivenessCheck=true` 时有效 |
| `result` | `originBase64` | `string` | 本次检测对应的相机原图 JPEG Data URL |
| `tips` | `code` | `number` | SDK 采集过程提示码 |
| `tips` | `message` | `string` | 提示码对应的可展示文案 |
| `error` | `code` | `string` | 错误码，包括权限、初始化、预览、帧处理和摄像头切换错误 |
| `error` | `message` | `string` | 错误详情 |
| `camera-change` | `cameraId` | `number` | 切换成功后的实际镜头：`0` 前置、`1` 后置；初始化降级到另一镜头时也会触发 |

### uni-app 兼容模式组件

兼容模式标签为 `face-ai-capture-compat`，仅用于 uni-app 的 app-nvue 或 uni-app x 的 VDOM app-uvue，不支持普通 Vue 页面、鸿蒙和 uni-app x 蒸汽模式：

```vue
<face-ai-capture-compat
  ref="captureCompatRef"
  style="width: 100%; height: 100%;"
  :performanceMode="1"
  :needLivenessCheck="true"
  :cameraId="0"
  :rotationDegrees="-1"
  :showFaceCover="false"
  @result="onResult"
/>
```

uni-app x 调用兼容模式组件方法时，需要使用组件生成的原生 Element 类型，不能使用普通 Vue 组件的 `$callMethod`：

```ts
import { FaceAiCaptureCompatElement } from "uts.sdk.modules.FaceAISearch"

const captureCompatRef = ref<FaceAiCaptureCompatElement | null>(null)

function retryCapture() {
  captureCompatRef.value?.retry()
}

function toggleCamera() {
  captureCompatRef.value?.toggleCamera()
}
```

相机预览使用居中裁剪方式铺满组件；当相机画面和组件宽高比不一致时，两侧画面可能被裁掉，但不会再出现上下黑边。

需要显示圆形遮罩和原生提示时，将 `showFaceCover` 设为 `true`。标准模式模板中可写为 `:show-face-cover="true"`，兼容模式可写为 `:showFaceCover="true"`。

### 横竖屏与平板适配

持续抓拍 API 传入 `rotationDegrees=-1` 时，CameraX 会跟随当前显示方向动态更新预览与分析帧；标准组件和兼容组件默认使用该模式。只有定制设备的摄像头方向与系统报告不一致时，才建议显式传入 `0/90/180/270`。

uni-app x 项目需要在 `pages.json` 的 `globalStyle` 或相机所在页面的 `style` 中允许自动旋转：

```json
{
  "globalStyle": {
    "pageOrientation": "auto"
  }
}
```

传统 uni-app 项目还需要在 `manifest.json` 中声明 App 支持的方向：

```json
{
  "app-plus": {
    "screenOrientation": [
      "portrait-primary",
      "portrait-secondary",
      "landscape-primary",
      "landscape-secondary"
    ]
  }
}
```

页面布局应根据可用窗口宽高自适应，不要为相机组件使用按屏幕宽度放大的固定 `rpx` 高度。Android 平板还应测试运行中旋转、分屏调整大小以及横屏正反方向。

Android uvue 接收到的兼容模式事件参数是 `Map<string, any>`，可通过 `result.get("croppedBase64")`、`result.get("silentScore")`、`result.get("originBase64")` 读取；uni-app nvue 中按普通事件对象读取。

兼容模式的 `tips`、`error`、`camera-change` 也使用 `Map<string, any>`，字段名与上表一致；标准模式事件直接返回对应的 UTS 类型对象。

> 两张图片均进行 Base64 编码，单次结果数据量较大。持续场景中请及时消费结果，不要长期把所有 Base64 字符串保存在响应式数组里。
