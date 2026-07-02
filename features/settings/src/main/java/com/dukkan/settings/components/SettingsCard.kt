package com.dukkan.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dukkan.settings.R
import com.msayeh.domain.model.AppCurrency
import com.msayeh.domain.model.AppLanguage
import com.msayeh.domain.model.ThemeMode

@Composable
fun SettingsCard(
    themeMode: ThemeMode,
    currency: AppCurrency,
    language: AppLanguage,
    onThemeSelected: (ThemeMode) -> Unit,
    onCurrencySelected: (AppCurrency) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Resolve labels up-front: SegmentedChips' label lambda is not @Composable.
    val lightLabel = stringResource(R.string.profile_light)
    val darkLabel = stringResource(R.string.profile_dark)
    val englishLabel = stringResource(R.string.profile_language_english)
    val arabicLabel = stringResource(R.string.profile_language_arabic)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            SettingRow(title = stringResource(R.string.profile_appearance)) {
                SegmentedChips(
                    options = listOf(ThemeMode.LIGHT, ThemeMode.DARK),
                    selected = if (themeMode == ThemeMode.DARK) ThemeMode.DARK else ThemeMode.LIGHT,
                    label = { mode -> if (mode == ThemeMode.DARK) darkLabel else lightLabel },
                    onSelect = onThemeSelected,
                )
            }

            CardDivider()

            SettingRow(
                title = stringResource(R.string.profile_currency),
                subtitle = stringResource(R.string.profile_currency_subtitle),
            ) {
                SegmentedChips(
                    options = AppCurrency.entries,
                    selected = currency,
                    label = { it.code },
                    onSelect = onCurrencySelected,
                )
            }

            CardDivider()

            SettingRow(title = stringResource(R.string.profile_language)) {
                SegmentedChips(
                    options = AppLanguage.entries,
                    selected = language,
                    label = { lang -> if (lang == AppLanguage.ARABIC) arabicLabel else englishLabel },
                    onSelect = onLanguageSelected,
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing()
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    )
}
