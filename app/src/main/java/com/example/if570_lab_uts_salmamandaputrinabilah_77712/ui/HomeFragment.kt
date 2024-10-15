package com.example.if570_lab_uts_salmamandaputrinabilah_77712.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.if570_lab_uts_salmamandaputrinabilah_77712.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity

class HomeFragment : Fragment() {

    private lateinit var currentDate: TextView
    private lateinit var currentTime: TextView
    private lateinit var checkoutStatus: TextView
    private lateinit var buttonAttendance: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 200

    private var capturedImageBitmap: Bitmap? = null // To hold the captured image temporarily

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        currentDate = view.findViewById(R.id.currentDate)
        currentTime = view.findViewById(R.id.currentTime)
        buttonAttendance = view.findViewById(R.id.buttonAttendance)
        checkoutStatus = view.findViewById(R.id.checkoutStatus)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set current date
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        currentDate.text = dateFormat.format(Date())

        // Set current time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        currentTime.text = timeFormat.format(Date()) // Use 'text' instead of 'append'

        // Load checkout status
        loadCheckoutStatus() // Call to load the checkout status at fragment creation

        buttonAttendance.setOnClickListener {
            checkAttendanceStatus()
        }
        return view
    }

    private fun loadCheckoutStatus() {
        val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        firestore.collection("attendance")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("date", currentDateString)
            .get()
            .addOnSuccessListener { documents ->
                var hasCheckedOut = false
                documents.forEach { document ->
                    if (document.contains("checkoutTime") && document.getString("checkoutTime") != null) {
                        hasCheckedOut = true
                        val checkoutTime = document.getString("checkoutTime")
                        checkoutStatus.text = "Checked out at: $checkoutTime" // Update the checkout status
                        checkoutStatus.visibility = View.VISIBLE // Make it visible
                    }
                }
                // If the user has not checked out, set the status to hidden
                if (!hasCheckedOut) {
                    checkoutStatus.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load checkout status: ${e.message}")
                Toast.makeText(requireContext(), "Failed to load checkout status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAttendanceStatus() {
        val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        firestore.collection("attendance")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("date", currentDateString)
            .get()
            .addOnSuccessListener { documents ->
                val hasCheckedIn = documents.any { it.contains("time") && it.getString("time") != null }
                val hasCheckedOut = documents.any { it.contains("checkoutTime") && it.getString("checkoutTime") != null }

                when {
                    hasCheckedIn && !hasCheckedOut -> {
                        // User has checked in but not checked out, prompt for check-out
                        showCheckoutDialog()
                    }
                    hasCheckedIn && hasCheckedOut -> {
                        // User has already checked out today
                        Toast.makeText(requireContext(), "You have already checked out today. Please check in tomorrow.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // User has not checked in yet, proceed to check-in
                        showAttendanceOptions()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to check attendance: ${e.message}")
                Toast.makeText(requireContext(), "Failed to check attendance", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAttendanceOptions(){
        val options = arrayOf("Take a Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> { // Take a photo
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE
                        )
                    } else {
                        openCamera()
                    }
                }
                1 -> { // Choose from gallery
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            GALLERY_REQUEST_CODE
                        )
                    } else {
                        openGallery()
                    }
                }
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            capturedImageBitmap = data?.extras?.get("data") as? Bitmap
            if (capturedImageBitmap != null) {
                showConfirmationDialog() // Show dialog for confirmation
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri)
                capturedImageBitmap = BitmapFactory.decodeStream(inputStream)
                showConfirmationDialog() // Show dialog for confirmation
            } else {
                Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Action canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Confirm Attendance")
        dialogBuilder.setMessage("Do you want to upload this photo?")

        // Show the captured image in the dialog
        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(capturedImageBitmap)
        dialogBuilder.setView(imageView)

        dialogBuilder.setPositiveButton("Upload") { dialog, _ ->
            // Save attendance record
            if (capturedImageBitmap != null) {
                uploadImageToStorage(capturedImageBitmap!!) // Proceed to upload for check-in
            }
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Retake") { dialog, _ ->
            openCamera() // Retake photo
            dialog.dismiss()
        }

        dialogBuilder.setNeutralButton("Delete") { dialog, _ ->
            Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT).show()
            capturedImageBitmap = null // Clear the captured image
            dialog.dismiss()
        }

        dialogBuilder.show()
    }

    private fun uploadImageToStorage(image: Bitmap) {
        // Convert Bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        // Create a unique filename for the image
        val imageRef = storage.reference.child("attendance_images/${System.currentTimeMillis()}.jpg")

        // Upload the image to Firebase Storage
        val uploadTask = imageRef.putBytes(imageBytes)
        uploadTask.addOnSuccessListener {
            // Get the download URL and save to Firestore
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveAttendanceData(uri.toString())
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseStorageError", "Failed to upload image: ${e.message}")
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAttendanceData(imageUrl: String) {
        // Create an attendance record
        val attendanceRecord = hashMapOf(
            "userId" to auth.currentUser?.uid,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            "time" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            "imageUrl" to imageUrl
        )

        // Save to Firestore
        firestore.collection("attendance").add(attendanceRecord)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Attendance recorded successfully", Toast.LENGTH_SHORT).show()
                loadCheckoutStatus() // Reload checkout status
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to save attendance: ${e.message}")
                Toast.makeText(requireContext(), "Failed to save attendance", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCheckoutDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Confirm Checkout")
        dialogBuilder.setMessage("Do you want to check out for today?")

        dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            performCheckout()
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.show()
    }

    private fun performCheckout() {
        val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestore.collection("attendance")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("date", currentDateString)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentRef = firestore.collection("attendance").document(document.id)

                    val checkoutTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    documentRef.update("checkoutTime", checkoutTime)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Checked out successfully at $checkoutTime", Toast.LENGTH_SHORT).show()
                            loadCheckoutStatus()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Failed to update checkout time: ${e.message}")
                            Toast.makeText(requireContext(), "Failed to check out", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to retrieve attendance record: ${e.message}")
                Toast.makeText(requireContext(), "Failed to check out", Toast.LENGTH_SHORT).show()
            }
    }
}
