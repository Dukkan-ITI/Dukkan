package com.dukkan.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dukkan.app.ui.theme.DukkanTheme
import com.dukkan.onboarding.view.OnboardingView


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DukkanTheme {
                OnboardingView(
                    onFinish = {

                    }
                )
            }
        }
    }
}



