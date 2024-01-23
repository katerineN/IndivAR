package com.raywenderlich.android.targetpractice

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startButton: Button = findViewById(R.id.startButton)
        val scoreboardButton: Button = findViewById(R.id.scoreboardButton)

        startButton.setOnClickListener {
            // Создаем интент для перехода на GameActivity
            val intent = Intent(this@MainActivity, GameActivity::class.java)

            // Запускаем GameActivity
            startActivity(intent)
        }

        // Устанавливаем обработчик событий для кнопки "Scoreboard"
        scoreboardButton.setOnClickListener {
            // Создаем интент для перехода на ScoreboardActivity
            val intent = Intent(this@MainActivity, ScoreboardActivity::class.java)

            // Запускаем ScoreboardActivity
            startActivity(intent)
        }
    }
}

