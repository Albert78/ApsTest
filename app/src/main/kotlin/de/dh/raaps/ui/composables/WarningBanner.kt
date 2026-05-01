package de.dh.eventseries.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.dh.raaps.ui.screens.common.Icon_Warning
import de.dh.raaps.ui.theme.ExtendedTheme

@Composable
fun WarningBanner(
    warningText: String,
    actionText: String? = null,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .clickable { onActionClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icon_Warning,
            contentDescription = null,
            tint = ExtendedTheme.semanticColors.warning,
            modifier = Modifier
                .size(50.dp)
                .padding(end = 10.dp)
        )

        Column {
            Text(
                text = warningText,
                style = MaterialTheme.typography.bodyMedium
            )

            if (actionText != null) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp, end = 20.dp)
                        .align(Alignment.End)
                )
            }
        }
    }
}

