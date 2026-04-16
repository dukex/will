package tech.benedicta.will.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import tech.benedicta.will.ui.theme.WillTextTertiary
import tech.benedicta.will.ui.theme.Spacing

@Composable
fun SectionHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = WillTextTertiary,
        modifier = modifier
            .padding(
                start = Spacing.xl,
                end = Spacing.xl,
                top = Spacing.sm,
                bottom = Spacing.xs,
            )
            .semantics { heading() }
    )
}
