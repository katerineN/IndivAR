package com.raywenderlich.android.targetpractice.data

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.targetpractice.R

class ScoreBoardAdapter(private val scoreboardItems: MutableList<ScoreBoardItem>) :
    RecyclerView.Adapter<ScoreBoardAdapter.ScoreboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scoreboard_item, parent, false)
        return ScoreboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreboardViewHolder, position: Int) {
        val scoreboardItem = scoreboardItems.getOrNull(position)
        scoreboardItem?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return scoreboardItems.size
    }


    inner class ScoreboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val throwsTextView: TextView = itemView.findViewById(R.id.throwsTextView)

        fun bind(scoreboardItem: ScoreBoardItem) {
            playerNameTextView.text = scoreboardItem.playerName
            timeTextView.text = scoreboardItem.time
            throwsTextView.text = "Throws: ${scoreboardItem.throws}"
        }
    }
}

// Класс для сравнения списков с использованием DiffUtil
class ScoreBoardDiffCallback(
    private val oldList: List<ScoreBoardItem>,
    private val newList: List<ScoreBoardItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

