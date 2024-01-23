/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.targetpractice

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import java.util.Random
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class GameActivity : AppCompatActivity() {

    // Сцена и камера для AR
    private lateinit var scene: Scene
    private lateinit var camera: Camera

    // Модель пули
    private lateinit var bulletRenderable: ModelRenderable

    // Флаг для старта таймера
    private var shouldStartTimer = true

    // Количество оставшихся воздушных шаров
    private var balloonsLeft = 20

    // Точка на экране для определения места выстрела
    private lateinit var point: Point

    // Текстовое поле для отображения количества оставшихся шаров
    private lateinit var balloonsLeftTxt: TextView

    // Звуковой пул
    private lateinit var soundPool: SoundPool
    private var sound = 0

    // Хранилище настроек
    private lateinit var sharedPreferences: SharedPreferences

    // Имя игрока
    private lateinit var playerName: String

    // Время старта игры
    private var startTimeMillis: Long = 0

    // Счетчик бросков
    private var throwsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Получение размеров экрана
        val display: Display = windowManager.defaultDisplay
        point = Point()
        display.getRealSize(point)

        // Установка макета активности
        setContentView(R.layout.activity_game)

        // Загрузка звукового пула
        loadSoundPool()

        // Инициализация элементов интерфейса и сцены AR
        balloonsLeftTxt = findViewById(R.id.balloonsCntTxt)
        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as GameFragment
        scene = arFragment.arSceneView.scene
        camera = scene.camera

        // Добавление воздушных шаров на сцену и создание модели пули
        addBalloonsToScene()
        buildBulletModel()

        // Установка слушателя событий для кнопки "Shoot"
        val shoot: Button = findViewById(R.id.shootButton)
        shoot.setOnClickListener {
            if (shouldStartTimer) {
                startTimer()
                shouldStartTimer = false
            }
            shoot()
        }
    }

    // Метод для загрузки звукового пула
    private fun loadSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        sound = soundPool.load(this, R.raw.blop_sound, 1)
    }

    // Метод вызывается при уничтожении активности
    override fun onDestroy() {
        // Сохранение результатов игры
        saveResults()
        super.onDestroy()
    }

    // Метод для отображения диалога ввода имени
    private fun showNameInputDialog() {
        val inputDialog = AlertDialog.Builder(this)
        val inputView = EditText(this)
        inputDialog.setView(inputView)
        inputDialog.setTitle("Enter Your Name")

        inputDialog.setPositiveButton("OK") { _, _ ->
            playerName = inputView.text.toString()
            saveResults()
            navigateToMainMenu()
        }

        inputDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        inputDialog.show()
    }

    // Метод для сохранения результатов игры в SharedPreferences
    @SuppressLint("MutatingSharedPrefs")
    private fun saveResults() {
        // Рассчитываем количество бросков и время
        val totalThrows = throwsCount
        val timeSpent = System.currentTimeMillis() - startTimeMillis
        val minutesPassed = timeSpent / (60 * 1000)
        val secondsPassed = (timeSpent % (60 * 1000)) / 1000

        // Формируем строку с результатами
        val resultString = "Player: $playerName\nThrows: $totalThrows\nTime: $minutesPassed:$secondsPassed |"
        Log.d("RESS", resultString)

        // Получаем предыдущие результаты из SharedPreferences
        val previousResults = sharedPreferences.getStringSet("results", mutableSetOf()) ?: mutableSetOf()

        // Объединяем новый результат с предыдущими
        previousResults.add(resultString)

        // Сохраняем новые результаты в SharedPreferences
        with(sharedPreferences.edit()) {
            putStringSet("results", previousResults)
            apply()
        }
    }

    // Метод для обработки выстрела
    private fun shoot() {
        // Увеличение счетчика бросков
        throwsCount++

        // Создание луча для определения точки выстрела
        val ray: Ray = camera.screenPointToRay(point.x / 2f, point.y / 2f)
        val node = Node()
        node.renderable = bulletRenderable
        scene.addChild(node)

        // Запуск потока для обработки выстрела
        Thread {
            for (i in 0 until 200) {
                runOnUiThread {
                    val vector3: Vector3 = ray.getPoint(i * 0.1f)
                    node.worldPosition = vector3

                    // Проверка пересечения пули с воздушным шаром
                    val nodeInContact: Node? = scene.overlapTest(node)
                    if (nodeInContact != null) {
                        balloonsLeft--
                        balloonsLeftTxt.text = "Balloons Left: $balloonsLeft"
                        scene.removeChild(nodeInContact)
                        soundPool.play(sound, 1f, 1f, 1, 0, 1f)
                    }
                }
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            runOnUiThread { scene.removeChild(node) }
        }.start()
    }

    // Метод для запуска таймера игры
    private fun startTimer() {
        startTimeMillis = System.currentTimeMillis()

        val timer: TextView = findViewById(R.id.timerText)
        Thread {
            var seconds = 0
            while (balloonsLeft > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                seconds++
                val minutesPassed = seconds / 60
                val secondsPassed = seconds % 60
                runOnUiThread { timer.text = "$minutesPassed:$secondsPassed" }
            }
            runOnUiThread {
                showNameInputDialog()
            }
        }.start()
    }

    // Метод для перехода в главное меню
    private fun navigateToMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность, чтобы пользователь не мог вернуться на нее кнопкой "назад"
    }

    // Метод для создания модели пули
    private fun buildBulletModel() {
        // Загрузка текстуры
        val textureBuilder = Texture.builder()
            .setSource(this, R.drawable.texture)

        textureBuilder.build().thenAccept { texture ->
            // Создание материала с текстурой
            val materialBuilder = MaterialFactory.makeOpaqueWithTexture(this, texture)
            materialBuilder.thenAccept { material ->
                // Создание модели пули
                bulletRenderable = ShapeFactory.makeSphere(0.01f, Vector3(0f, 0f, 0f), material)
            }
        }
    }

    // Метод для добавления воздушных шаров на сцену
    private fun addBalloonsToScene() {
        // Загрузка модели воздушного шара
        ModelRenderable.builder()
            .setSource(this, Uri.parse("balloon.sfb"))
            .build()
            .thenAccept { renderable ->
                // Создание нескольких экземпляров воздушных шаров на сцене
                for (i in 0 until 20) {
                    val node = Node()
                    node.renderable = renderable
                    scene.addChild(node)

                    // Рандомные координаты для размещения шаров
                    val random = Random()
                    val x = random.nextInt(10)
                    val z = -random.nextInt(10)
                    val y = random.nextInt(20)

                    node.worldPosition = Vector3(
                        x.toFloat(),
                        y / 10f,
                        z.toFloat()
                    )
                }
            }
    }
}
