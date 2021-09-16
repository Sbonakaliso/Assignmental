package com.example.android.modules

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AssignmentAdapter(inputAssignmentList: ArrayList<Assignment>, var interactionListener: InteractionListener): RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    private val assignmentList = inputAssignmentList

    class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val ibtnDelete: ImageButton = itemView.findViewById(R.id.ibtnDelete)
        val ibtnEdit: ImageButton = itemView.findViewById(R.id.ibtnEdit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.each_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val name = assignmentList[position].name
        val date = assignmentList[position].date
        val progress = assignmentList[position].progress
        val progressString = assignmentList[position].progress.toString() + "% Done"

        holder.tvName.text = name
        holder.tvDate.text = date
        holder.progressBar.progress = progress!!
        holder.tvProgress.text = progressString
        if(progress == 100){
            holder.tvProgress.setTextColor(Color.rgb(0, 153, 0))
            holder.tvProgress.typeface = Typeface.DEFAULT_BOLD
        }else {
            holder.tvProgress.setTextColor(Color.GRAY)
            holder.tvProgress.typeface = Typeface.DEFAULT
        }
        holder.ibtnEdit.setOnClickListener {
            interactionListener.onEdit(position)
        }
        holder.ibtnDelete.setOnClickListener {
            interactionListener.onDelete(position)
        }

    }

    override fun getItemCount(): Int {
        return assignmentList.size
    }
    interface InteractionListener{
        fun onDelete(position: Int)
        fun onEdit(position: Int)
    }

}