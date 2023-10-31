package com.example.enhancedclient

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.enhancedclient.api.DynamoApi
import com.example.enhancedclient.databinding.ActivityMainBinding
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // レイアウト紐付け
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ログインボタン押下時の設定
        binding.loginButton.setOnClickListener {
            val userId: String = binding.userId.text.toString()
            val password: String = binding.password.text.toString()

            // 未入力欄がある場合
            if (userId.isBlank() || password.isBlank()) {
                showLongToast(R.string.inputUserInfo)
                return@setOnClickListener
            }

            // 外部通信のためスレッド生成
            val queryDynamoDb = Thread {
                try {
                    // Query結果
                    val dynamoApi = DynamoApi()
                    val userRecord = dynamoApi.getDoesExistUser(userId, password)
                    if (userRecord) {
                        // User情報が登録済みの場合
                        showLongToast(R.string.loginSuccess)
                        // ログイン後後続処理はこの下に記載
                    } else {
                        // User情報がない場合
                        showLongToast(R.string.loginFailed)
                    }
                } catch (error: DynamoDbException) {
                    showLongToast(R.string.unknownError)
                }
            }
            queryDynamoDb.start()
            try {
                queryDynamoDb.join()
            } catch (error: InterruptedException) {
                Toast.makeText(this, R.string.unknownError, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLongToast(messageInt: Int) {
        // 別スレッドからUIを操作
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(this, messageInt, Toast.LENGTH_LONG).show()
        }
    }
}