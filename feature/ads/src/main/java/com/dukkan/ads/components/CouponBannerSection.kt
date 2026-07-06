package com.dukkan.ads.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.ads.R
import com.dukkan.ads.viewmodel.CouponBannerViewModel
import com.dukkan.design_system.components.GuestAuthDialog
import com.dukkan.domain.model.Coupon

@Composable
fun CouponBannerSection(
    modifier: Modifier = Modifier,
    viewModel: CouponBannerViewModel = hiltViewModel(),
    onShopClick: (Coupon) -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val pagerState = rememberPagerState(
        pageCount = { state.coupons.size }
    )

    Box(modifier = modifier.fillMaxWidth()) {

        Column {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                pageSpacing = 12.dp
            ) { page ->
                CouponBannerCard(
                    coupon = state.coupons[page],
                    onSaveClick = {
                        viewModel.onCouponClick(state.coupons[page])
                    },
                    onShopClick = {
                        onShopClick(state.coupons[page])
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                repeat(state.coupons.size) { index ->

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                if (pagerState.currentPage == index)
                                    10.dp
                                else
                                    7.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.LightGray
                            )
                    )
                }
            }
        }

        if (state.showGuestDialog) {
            GuestAuthDialog(
                onDismiss = viewModel::dismissGuestDialog,
                onSignInClick = onSignInClick
            )
        }

        AnimatedVisibility(
            visible = state.savedSuccess,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.coupon_saved),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

