package com.checo.techforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.checo.techforge.data.TechForgeDatabase
import com.checo.techforge.data.TechForgeRepository
import com.checo.techforge.ui.TechForgeApp
import com.checo.techforge.ui.TechForgeTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        TechForgeRepository(TechForgeDatabase.getInstance(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TechForgeTheme {
                TechForgeApp(repository)
            }
        }
    }
}
