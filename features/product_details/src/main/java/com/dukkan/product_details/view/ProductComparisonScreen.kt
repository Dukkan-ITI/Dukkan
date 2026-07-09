package com.dukkan.product_details.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dukkan.design_system.components.ErrorScreen
import com.dukkan.design_system.components.FilterChip
import com.dukkan.design_system.components.PrimaryButton
import com.dukkan.design_system.util.shimmerLoadingAnimation
import com.dukkan.domain.model.ComparisonFeature
import com.dukkan.domain.model.ProductComparisonResult
import com.dukkan.product_details.R
import com.dukkan.product_details.viewmodel.ProductComparisonViewModel
import kotlinx.coroutines.delay

private enum class ComparisonPhase { Loading, Question, Result, Error }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductComparisonScreen(
    onBackClick: () -> Unit = {},
    viewModel: ProductComparisonViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val phase = when {
        state.isLoading -> ComparisonPhase.Loading
        state.error != null || state.result is ProductComparisonResult.Error -> ComparisonPhase.Error
        state.result is ProductComparisonResult.NeedsMoreInfo -> ComparisonPhase.Question
        state.result is ProductComparisonResult.Comparison -> ComparisonPhase.Result
        else -> ComparisonPhase.Loading
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.ai_product_comparison),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.comparison_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 12 })
                    .togetherWith(fadeOut(tween(200)))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            label = "comparison-phase"
        ) { target ->
            when (target) {
                ComparisonPhase.Loading -> AiLoadingState()
                ComparisonPhase.Error -> {
                    val message = state.error
                        ?: (state.result as? ProductComparisonResult.Error)?.message
                        ?: stringResource(R.string.comparison_error_generic)
                    ErrorScreen(
                        message = message,
                        onRetry = { viewModel.retry() },
                        retryText = stringResource(R.string.retry)
                    )
                }
                ComparisonPhase.Question -> {
                    val info = state.result as ProductComparisonResult.NeedsMoreInfo
                    AiQuestionState(
                        info = info,
                        answerInput = state.answerInput,
                        onAnswerChanged = viewModel::onAnswerInputChanged,
                        onSubmit = viewModel::submitAnswer
                    )
                }
                ComparisonPhase.Result -> {
                    val comparison = state.result as ProductComparisonResult.Comparison
                    ComparisonResultContent(
                        comparison = comparison,
                        image1Url = state.product1?.images?.firstOrNull()?.url,
                        image2Url = state.product2?.images?.firstOrNull()?.url,
                        title1 = state.product1?.title,
                        title2 = state.product2?.title
                    )
                }
            }
        }
    }
}

@Composable
private fun AiLoadingState() {
    val statusMessages = listOf(
        stringResource(R.string.comparison_analyzing),
        stringResource(R.string.comparison_status_comparing_specs),
        stringResource(R.string.comparison_status_weighing),
        stringResource(R.string.comparison_status_recommendation)
    )
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            index = (index + 1) % statusMessages.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        AnimatedAiIcon(isLoading = true, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(20.dp))
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "loading-status"
        ) { i ->
            Text(
                text = statusMessages[i],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(modifier = Modifier.weight(1f).height(120.dp))
            Spacer(modifier = Modifier.width(16.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(120.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        repeat(3) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .shimmerLoadingAnimation()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiQuestionState(
    info: ProductComparisonResult.NeedsMoreInfo,
    answerInput: String,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedAiIcon(isLoading = false, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = info.question.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.suggested_answers),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            info.question.options.forEachIndexed { i, option ->
                val transitionState = remember {
                    MutableTransitionState(false).apply { targetState = true }
                }
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(250, delayMillis = 60 * i)) +
                        slideInVertically(tween(250, delayMillis = 60 * i)) { it / 3 }
                ) {
                    FilterChip(
                        label = option,
                        isSelected = false,
                        onClick = {
                            onAnswerChanged(option)
                            onSubmit()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.comparison_or),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = answerInput,
            onValueChange = onAnswerChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.type_answer_here)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(
            text = stringResource(R.string.submit),
            onClick = onSubmit,
            enabled = answerInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ComparisonResultContent(
    comparison: ProductComparisonResult.Comparison,
    image1Url: String?,
    image2Url: String?,
    title1: String?,
    title2: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        comparison.recommendationText?.let { text ->
            item { AiRecommendationCard(text) }
        }

        if (title1 != null && title2 != null) {
            item {
                VersusHeader(
                    image1Url = image1Url,
                    image2Url = image2Url,
                    title1 = title1,
                    title2 = title2,
                    winner = comparison.overallWinner
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.detailed_comparison),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(comparison.features) { index, feature ->
            val transitionState = remember {
                MutableTransitionState(false).apply { targetState = true }
            }
            val delay = (index * 50).coerceAtMost(400)
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(tween(300, delayMillis = delay)) +
                    slideInVertically(tween(300, delayMillis = delay)) { it / 4 }
            ) {
                ComparisonFeatureCard(feature)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AiRecommendationCard(text: String) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedAiIcon(isLoading = false, modifier = Modifier.size(28.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VersusHeader(
    image1Url: String?,
    image2Url: String?,
    title1: String,
    title2: String,
    winner: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductImageWithWinnerBadge(
            imageUrl = image1Url,
            title = title1,
            isWinner = winner == 1,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.comparison_vs),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ProductImageWithWinnerBadge(
            imageUrl = image2Url,
            title = title2,
            isWinner = winner == 2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProductImageWithWinnerBadge(
    imageUrl: String?,
    title: String,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isWinner) { visible = isWinner }
    val badgeScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "winner-badge"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .then(
                        if (isWinner) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        ) else Modifier
                    ),
                contentScale = ContentScale.Crop
            )

            if (isWinner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .graphicsLayer {
                            scaleX = badgeScale
                            scaleY = badgeScale
                        }
                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.comparison_winner),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ComparisonFeatureCard(feature: ComparisonFeature) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = feature.featureName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonValueCell(
                    value = feature.product1Value,
                    isWinner = feature.winner == 1,
                    modifier = Modifier.weight(1f)
                )
                ComparisonValueCell(
                    value = feature.product2Value,
                    isWinner = feature.winner == 2,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ComparisonValueCell(
    value: String,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val cellModifier = if (isWinner) {
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), shape)
    } else {
        modifier
    }
    Row(
        modifier = cellModifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isWinner) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.comparison_winner),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
        )
    }
}
