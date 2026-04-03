package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.FestivalTextField

@Composable
internal fun ProfileEditableFieldRow(
    label: String,
    value: String,
    textFieldValue: String,
    fieldTag: String,
    editButtonTag: String,
    isEditing: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit,
    onStartEditing: () -> Unit,
    badge: (@Composable () -> Unit)? = null,
) {
    if (isEditing) {
        FestivalTextField(
            value = textFieldValue,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(fieldTag),
            isError = errorMessage != null,
            supportingText = errorMessage,
            keyboardOptions = keyboardOptions,
        )
        return
    }

    ProfileValueRow(
        label = label,
        value = value,
        editButtonTag = editButtonTag,
        onEdit = onStartEditing,
        badge = badge,
    )
}

@Composable
internal fun ProfileReadOnlyFieldRow(
    label: String,
    value: String,
    badge: (@Composable () -> Unit)? = null,
) {
    ProfileValueRow(
        label = label,
        value = value,
        editButtonTag = null,
        onEdit = null,
        badge = badge,
    )
}

@Composable
private fun ProfileValueRow(
    label: String,
    value: String,
    editButtonTag: String?,
    onEdit: (() -> Unit)?,
    badge: (@Composable () -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    badge?.invoke()
                }
            }
            if (onEdit != null && editButtonTag != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag(editButtonTag),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Modifier $label",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}
