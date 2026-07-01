package com.dukkan.auth.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.auth.R
import com.example.design_system.theme.AppTheme


@Composable
fun AuthVerificationHint(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        CheckmarkIcon(
            tint = accentColor,
            modifier = Modifier
                .size(15.dp)
                .padding(top = 1.dp),
        )
        Text(
            text = stringResource(R.string.auth_register_verification_hint),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.4.sp,
        )
    }
}

@Composable
private fun CheckmarkIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val sx = size.width / 24f
        val sy = size.height / 24f
        val path = Path().apply {
            moveTo(4 * sx, 12 * sy)
            lineTo(9 * sx, 17 * sy)
            lineTo(20 * sx, 6 * sy)
        }
        drawPath(path = path, color = tint, style = stroke)
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthVerificationHintPreview() {
    AppTheme {
        AuthVerificationHint(modifier = Modifier.padding(16.dp))
    }
}