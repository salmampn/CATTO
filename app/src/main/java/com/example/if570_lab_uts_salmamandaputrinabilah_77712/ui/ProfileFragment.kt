package com.example.if570_lab_uts_salmamandaputrinabilah_77712.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var editTextName: EditText
    private lateinit var editTextNIM: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonLogout: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        editTextName = view.findViewById(R.id.editTextName)
        editTextNIM = view.findViewById(R.id.editTextNIM)
        buttonSave = view.findViewById(R.id.buttonSave)
        buttonLogout = view.findViewById(R.id.buttonLogout) // Logout button

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // Initialize Firestore

        buttonSave.setOnClickListener { saveProfile() }
        buttonLogout.setOnClickListener { showLogoutConfirmation() } // Set logout action

        loadProfile() // Load profile when the fragment is opened

        return view
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val nim = document.getString("nim") ?: ""
                    editTextName.setText(name)
                    editTextNIM.setText(nim)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val name = editTextName.text.toString().trim()
        val nim = editTextNIM.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (name.isEmpty() || nim.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userProfile = hashMapOf(
            "name" to name,
            "nim" to nim
        )

        firestore.collection("users").document(userId).set(userProfile)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLogoutConfirmation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ -> logout() }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun logout() {
        auth.signOut() // Sign out from Firebase
        // Navigate back to LoginActivity
        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // Close the current activity
    }
}
