package com.sleeptracker.presentation.ui.components.transparency

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.privacy_high
import sleeptracker.composeapp.generated.resources.privacy_high_open
import sleeptracker.composeapp.generated.resources.privacy_medium
import sleeptracker.composeapp.generated.resources.privacy_medium_open
import sleeptracker.composeapp.generated.resources.privacy_low
import sleeptracker.composeapp.generated.resources.privacy_low_open

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PrivacyIcon(
    iconName: String,
    iconSize: Dp = 40.dp,
    isOpen: Boolean = false,
    modifier: Modifier = Modifier,
    onPress: () -> Unit
) {
    val painter = when (iconName) {
        "privacy_high"   -> if (isOpen) painterResource(Res.drawable.privacy_high_open)   else painterResource(Res.drawable.privacy_high)
        "privacy_medium" -> if (isOpen) painterResource(Res.drawable.privacy_medium_open) else painterResource(Res.drawable.privacy_medium)
        else             -> if (isOpen) painterResource(Res.drawable.privacy_low_open)    else painterResource(Res.drawable.privacy_low)
    }

    Box(
        modifier         = modifier
            .size(iconSize)
            .clickable(onClick = onPress),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painter,
            contentDescription = "Privacy risk: $iconName",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(iconSize)
        )
    }
}
