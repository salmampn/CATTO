package com.example.if570_lab_uts_salmamandaputrinabilah_77712.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.R
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceAdapter(private val attendanceList: List<Attendance>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.attendanceImage)
        private val dateTextView: TextView = itemView.findViewById(R.id.attendanceDate)
        private val timeTextView: TextView = itemView.findViewById(R.id.attendanceTime)

        fun bind(attendance: Attendance) {
            dateTextView.text = SimpleDateFormat("dd MMMM yyyy", Locale("en", "ID"))
                .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(attendance.date))
            timeTextView.text = attendance.time

            // Load the image using Glide
            Glide.with(itemView.context)
                .load(attendance.imageUrl)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendanceList[position])
    }

    override fun getItemCount(): Int = attendanceList.size
}

