package com.example.if570_lab_uts_salmamandaputrinabilah_77712.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.R
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.model.Attendance
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.model.AttendanceAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var attendanceAdapter: AttendanceAdapter
    private lateinit var attendanceList: MutableList<Attendance>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        attendanceList = mutableListOf()
        attendanceAdapter = AttendanceAdapter(attendanceList)
        recyclerView.adapter = attendanceAdapter

        firestore = Firebase.firestore
        getAttendanceRecords()
    }

    private fun getAttendanceRecords() {
        // Ambil ID pengguna yang sedang login
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Cek apakah userId tidak null
        if (userId != null) {
            firestore.collection("attendance")
                .whereEqualTo("userId", userId) // Filter berdasarkan userId
                .get()
                .addOnSuccessListener { result ->
//                    attendanceList.clear() // Bersihkan daftar sebelum menambahkan data baru
                    for (document in result) {
                        val attendance = document.toObject(Attendance::class.java)
                        attendanceList.add(attendance)
                    }
                    attendanceList.sortByDescending { it.date }
                    attendanceAdapter.notifyDataSetChanged() // Notify adapter about data changes
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
