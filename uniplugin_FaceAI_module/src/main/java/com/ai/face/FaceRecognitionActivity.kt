// FaceRecognitionActivity.kt
package com.ai.face

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FaceRecognitionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. 获取传递的参数
        val userId = intent.getStringExtra("userId") ?: ""
        val scanMode = intent.getBooleanExtra("scanMode", true)
        Toast.makeText(baseContext,"跳转", Toast.LENGTH_LONG).show();

        // 2. 设置布局（此处使用代码创建简单UI）
        val textView = TextView(this).apply {
            text = "人脸识别页面\n用户ID: $userId\n扫描模式: $scanMode"
            gravity = Gravity.CENTER
            textSize = 20f
        }

        setContentView(textView)
        
        // 3. 添加关闭按钮
        val closeButton = Button(this).apply {
            text = "关闭并返回结果"
            setOnClickListener {
                // 设置返回结果
                val resultIntent = Intent().apply {
                    putExtra("result", "识别成功")
                    putExtra("confidence", 0.95)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
        
        addContentView(closeButton, 
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}