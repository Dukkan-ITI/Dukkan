package com.dukkan.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dukkan.onboarding.R


@Composable
fun OnboardingBackground(image:Int){
    Image(
        painter = painterResource(image),
        contentDescription = stringResource(R.string.onboarding_image_description),
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}