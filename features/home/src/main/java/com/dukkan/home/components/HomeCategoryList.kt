package com.dukkan.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.home.R
import com.example.design_system.theme.AppTheme

@Composable
fun HomeCategoryList(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCategorySelected(category) }
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
                    text = category,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeCategoryListPreview() {
    AppTheme {
        HomeCategoryList(
            categories = listOf(
                stringResource(R.string.all),
                stringResource(R.string.new_in), stringResource(R.string.clothing),
                stringResource(R.string.shoes)
            ),
            selectedCategory = stringResource(R.string.all),
            onCategorySelected = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}