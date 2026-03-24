package com.projetmobile.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AuthFeedbackTone {
    Success,
    Error,
}

@Composable
fun GradientScreenBackground(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F6FD),
                        Color(0xFFE8EEF9),
                        Color(0xFFF7F9FF),
                    ),
                ),
            ),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Box(content = content)
    }
}

@Composable
fun FestivalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label) },
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { message ->
            { Text(text = message) }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFD0D8E8),
            errorBorderColor = Color(0xFFC62828),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color(0xFF5D6981),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDatePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Ouvre le dialogue au clic sur le champ (qui est en lecture seule)
    Box(modifier = modifier) {
        FestivalTextField(
            value = value,
            onValueChange = {},
            label = label,
            isError = isError,
            supportingText = supportingText,
            modifier = Modifier.fillMaxWidth(),
            enabled = false, // Désactivé pour forcer le clic sur la Box
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
            },
            // On utilise une Box invisible par dessus pour capturer le clic car le TextField disabled ne le fait pas bien
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        onValueChange(date.format(formatter))
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PrimaryAuthButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text = text)
    }
}

@Composable
fun AuthLinkButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        ) 
    }
}

@Composable
fun AuthFeedbackBanner(
    message: String,
    tone: AuthFeedbackTone,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (tone) {
        AuthFeedbackTone.Success -> Color(0xFFE3F6E9)
        AuthFeedbackTone.Error -> Color(0xFFFDE6E6)
    }
    val textColor = when (tone) {
        AuthFeedbackTone.Success -> Color(0xFF087443)
        AuthFeedbackTone.Error -> Color(0xFFB42318)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Text(
            text = message,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun InlineAuthLinkButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
