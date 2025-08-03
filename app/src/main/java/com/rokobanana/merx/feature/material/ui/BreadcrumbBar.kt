package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BreadcrumbBar(sections: List<String>) {
    Row(Modifier.padding(16.dp)) {
        sections.forEachIndexed { idx, section ->
            Text(section, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            if (idx != sections.lastIndex) {
                Text(" > ", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
    }
}