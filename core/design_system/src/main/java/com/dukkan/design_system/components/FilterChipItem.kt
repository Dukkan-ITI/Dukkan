package com.dukkan.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.design_system.R
import com.dukkan.design_system.theme.AppTheme

data class FilterChipItem(
    val id: String,
    val label: String
)

@Composable
fun FilterChipList(
    items: List<FilterChipItem>,
    selectedItemId: String?,
    onItemSelected: (FilterChipItem) -> Unit,
    modifier: Modifier = Modifier,
    showSeeAll: Boolean = false,
    seeAllLabel: String = stringResource(R.string.see_all_label),
    onSeeAllClicked: (() -> Unit)? = null
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            val isSelected = item.id == selectedItemId

            FilterChip(
                label = item.label,
                isSelected = isSelected,
                onClick = { onItemSelected(item) }
            )
        }

        if (showSeeAll && onSeeAllClicked != null) {
            item {
                SeeAllChip(
                    label = seeAllLabel,
                    onClick = onSeeAllClicked
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color.Transparent
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (isSelected) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SeeAllChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipListHomePreview() {
    AppTheme {
        FilterChipList(
            items = listOf(
                FilterChipItem("all", stringResource(R.string.filter_all)),
                FilterChipItem("new", stringResource(R.string.filter_new_in)),
                FilterChipItem("clothing", stringResource(R.string.filter_clothing)),
                FilterChipItem("shoes", stringResource(R.string.filter_shoes))
            ),
            selectedItemId = "all",
            onItemSelected = {},
            showSeeAll = true,
            seeAllLabel = stringResource(R.string.see_all_label),
            onSeeAllClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipListFilterPreview() {
    AppTheme {
        FilterChipList(
            items = listOf(
                FilterChipItem("nike", stringResource(R.string.brand_nike)),
                FilterChipItem("adidas", stringResource(R.string.brand_adidas)),
                FilterChipItem("puma", stringResource(R.string.brand_puma))
            ),
            selectedItemId = "nike",
            onItemSelected = {}
        )
    }
}