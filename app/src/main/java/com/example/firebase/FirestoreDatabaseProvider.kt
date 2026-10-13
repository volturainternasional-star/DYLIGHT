package com.example.firebase

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDatabaseProvider {
  fun get(context: Context): FirebaseFirestore {
    val dbId = context.getString(R.string.firestore_database_id)
    return FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
  }
}
