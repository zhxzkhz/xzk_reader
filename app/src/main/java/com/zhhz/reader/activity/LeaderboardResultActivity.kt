package com.zhhz.reader.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zhhz.reader.R
import com.zhhz.reader.ui.leaderboardresult.LeaderboardResultFragment

class LeaderboardResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500) // 设置状态栏颜色
        setContentView(R.layout.activity_leaderboard_result)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LeaderboardResultFragment())
                .commitNow()
        }
    }


}