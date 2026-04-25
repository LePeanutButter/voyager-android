package com.voyager.tourism.presentation.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyager.tourism.data.dto.TravelerSummaryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelerSummaryBottomSheet(
    summary: TravelerSummaryDto?,
    onDismiss: () -> Unit
) {
    if (summary == null) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = summary.displayName)
            Text(text = summary.bioShort)
            Text(text = "Intereses: ${summary.interests}")
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    }
}
