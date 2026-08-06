package com.example.arise

import android.app.Application
import com.example.arise.data.FirestoreSyncRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AriseApplication : Application() {
    @Inject
    lateinit var firestoreSyncRepository: FirestoreSyncRepository
}
