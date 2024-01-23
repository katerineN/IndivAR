package com.raywenderlich.android.targetpractice

// ScoreboardActivity.kt
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.targetpractice.data.ScoreBoardAdapter
import com.raywenderlich.android.targetpractice.data.ScoreBoardItem

class ScoreboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scoreboardAdapter: ScoreBoardAdapter
    private lateinit var results: MutableList<ScoreBoardItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)

        recyclerView = findViewById(R.id.recyclerView)

        // Загрузите результаты
        results = loadBestResults()
        Log.d("RES", results[0].toString())

        // Инициализируйте адаптер
        scoreboardAdapter = ScoreBoardAdapter(results)

        // Настройте RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = scoreboardAdapter
    }

    private fun loadResults(): MutableList<ScoreBoardItem> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val results = mutableListOf<ScoreBoardItem>()

        // Получаем строку с результатами из SharedPreferences
        val resultsSet = preferences.getStringSet("results", setOf())
        val resultsString = resultsSet.joinToString("\n")

        // Проходим по массиву результатов и извлекаем данные
        if (!resultsString.isNullOrEmpty()) {
            val resultStrings = resultsString.split("|").filter { it.isNotBlank() }
            Log.d("TAG", resultStrings[0])
            for (resultString in resultStrings) {
                var resultParts = resultString.split("\n").filter { it.isNotBlank() }
                //resultParts = resultParts.split(": ")
                if (resultParts.size >= 3) {
                    val name = resultParts[0].split(": ")[1]
                    val throws = resultParts[1].split(": ")[1].toIntOrNull() ?: 0
                    val time = resultParts[2].split(": ")[1]

                    results.add(ScoreBoardItem(name, time, throws))
                }
            }
        }

        return results
    }

    private fun loadBestResults(): MutableList<ScoreBoardItem> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val results = mutableListOf<ScoreBoardItem>()

        // Получаем строку с результатами из SharedPreferences
        val resultsSet = preferences.getStringSet("results", setOf())
        val resultsString = resultsSet.joinToString("\n")

        // Проходим по массиву результатов и извлекаем данные
        if (!resultsString.isNullOrEmpty()) {
            val resultStrings = resultsString.split("|").filter { it.isNotBlank() }
            val bestResultsMap = mutableMapOf<String, ScoreBoardItem>()

            for (resultString in resultStrings) {
                val resultParts = resultString.split("\n").filter { it.isNotBlank() }
                if (resultParts.size >= 3) {
                    val name = resultParts[0].split(": ")[1]
                    val throws = resultParts[1].split(": ")[1].toIntOrNull() ?: 0
                    val time = resultParts[2].split(": ")[1]

                    val existingBestResult = bestResultsMap[name]

                    if (existingBestResult == null ||
                        (throws < existingBestResult.throws) ||
                        (throws == existingBestResult.throws && time < existingBestResult.time)
                    ) {
                        bestResultsMap[name] = ScoreBoardItem(name, time, throws)
                    }
                }
            }

            results.addAll(bestResultsMap.values)
        }
        // Сортируем результаты от лучшего к худшему (по количеству бросков и времени)
        results.sortByDescending { it.throws }
        results.sortBy { it.time }
        return results
    }


}
