package com.dukkan.onboarding.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dukkan.onboarding.components.BottomSection

import com.dukkan.onboarding.components.GradientOverlay
import com.dukkan.onboarding.components.OnboardingBackground
import com.dukkan.onboarding.components.OnboardingPageContent
import com.dukkan.onboarding.components.TopBar
import com.dukkan.onboarding.model.OnboardingModel
import com.dukkan.onboarding.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingView(
    viewModel: OnboardingViewModel = viewModel(),
    onFinish: () -> Unit,
    onSignIn: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()

    OnboardingContent(
        pages = state.pages,
        onFinish = onFinish,
        onSignIn = onSignIn
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingContent(
    pages: List<OnboardingModel>,
    onFinish: () -> Unit,
    onSignIn: () -> Unit
) {

    val pagerState = rememberPagerState(
        pageCount = { pages.size }
    )

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            Box {

                OnboardingBackground(
                    image = pages[page].imageRes
                )

                GradientOverlay()

                OnboardingPageContent(
                    pageIndex = page,
                    page = pages[page]
                )
            }
        }

        TopBar(
            currentPage = pagerState.currentPage,
            pageCount = pages.size,
            onSkip = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pages.lastIndex)
                }
            }
        )

        BottomSection(
            modifier = Modifier.align(Alignment.BottomCenter),
            isLastPage = pagerState.currentPage == pages.lastIndex,
            onContinue = {

                if (pagerState.currentPage == pages.lastIndex) {
                    onFinish()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    }
                }

            },
            onSignInClick = onSignIn
        )
    }
}