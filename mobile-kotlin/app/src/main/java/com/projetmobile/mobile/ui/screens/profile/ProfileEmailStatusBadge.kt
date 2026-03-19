package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
internal fun EmailStatusBadge(emailVerified: Boolean) {
    val containerColor = if (emailVerified) Color(0xFFE4F7EA) else Color(0xFFFFF1D6)
    val textColor = if (emailVerified) Color(0xFF087443) else Color(0xFF9A6700)
    Surface(shape = RoundedCornerShape(999.dp), color = containerColor) {
        Text(
            text = if (emailVerified) "Vérifié" else "À vérifier",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
