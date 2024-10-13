## UTS - IF570BL - Mobile Application

- Salma Manda Putri Nabilah (00000077712) - [@salmampn](https://github.com/salmampn)

# CATTO - IF570BL (Lab)

CATTO is an application that allows users to perform attendance using photos and store data in Firebase. This application was created for the Midterm Exam of the IF570 Mobile Application course.

## Key Features

1. **Home Page:** Displays the current date and time, along with a button to perform attendance. Users can take a photo as proof of attendance.
2. **Camera for Attendance:** Allows users to open the camera and take a photo during attendance. Users can delete or retake the photo before uploading it.
3. **Attendance Restrictions:**
   - Users can perform one check-in and one check-out in a single day.
   - After checking in, users cannot check in again on the same day.
   - If users have checked out, they cannot check in again until the next day.
4. **Attendance History:** Displays all attendance data with the most recent records at the top. Attendance data from previous days will be shown below.
5. **Profile Page:** Users can enter their name and student ID (NIM), and save this information to Firebase.

## Installation

1. Clone this repository:

```bash
git clone https://github.com/salmampn/CATTO.git
```

2. Open the project in Android Studio.
3. Make sure to add Firebase to your project and configure the `google-services.json` file.
4. Run the application on an emulator or Android device.
   
## Technologies Used

- **Android SDK:** For developing the Android application.
- **Firebase:** For data storage and user authentication.
- **RecyclerView:** For displaying attendance history.
- **Glide:** For loading and caching images efficiently in the app.
  
## References

- [Android Developer Codelabs](https://developer.android.com/get-started/codelabs)
- [ChatGPT](https://chat.openai.com/) - For any problem issues.
