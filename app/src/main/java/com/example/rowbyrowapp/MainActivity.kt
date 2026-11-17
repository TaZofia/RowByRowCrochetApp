package com.example.rowbyrowapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rowbyrowapp.ui.editor.CrochetEditorScreen
import com.example.rowbyrowapp.ui.theme.RowByRowAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RowByRowAppTheme {
                CrochetEditorScreen()
            }
        }
    }
}

