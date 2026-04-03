package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class PrimaryAuthButtonTone {
    Primary,
    Accent,
}

@Composable
fun PrimaryAuthButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: PrimaryAuthButtonTone = PrimaryAuthButtonTone.Primary,
    onClick: () -> Unit,
) {
    val containerColor = when (tone) {
        PrimaryAuthButtonTone.Primary -> MaterialTheme.colorScheme.primary
        PrimaryAuthButtonTone.Accent -> MaterialTheme.colorScheme.secondary
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.82f),
        ),
    ) {
        Text(text = text)
    }
}
