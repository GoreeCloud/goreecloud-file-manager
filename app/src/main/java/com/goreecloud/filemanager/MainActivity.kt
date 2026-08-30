package com.goreecloud.filemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.goreecloud.filemanager.storage.LocalFileRepository
import com.goreecloud.filemanager.ui.FileManagerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localRepository = LocalFileRepository(filesDir)
        setContent {
            FileManagerApp(localRepository)
        }
    }
}
