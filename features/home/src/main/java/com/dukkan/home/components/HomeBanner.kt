//package com.dukkan.home.components
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.dukkan.home.R
//import com.dukkan.design_system.theme.AppTheme
//
//@Composable
//fun HomeBanner(
//    modifier: Modifier = Modifier,
//    onShopClick: () -> Unit = {}
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 20.dp, top = 18.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.Bottom
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp)
//                .clip(RoundedCornerShape(28.dp))
//                .background(MaterialTheme.colorScheme.primary)
//        ) {
//            Image(
//                painter = painterResource(com.dukkan.design_system.R.drawable.banner_placeholder),
//                contentDescription = stringResource(R.string.summer_drop),
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(206.dp)
//            )
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 18.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Bottom
//            ) {
//                Column {
//                    Text(
//                        text = stringResource(R.string.summer_capsule),
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Bold,
//                        letterSpacing = 1.5.sp,
//                        color = DukkanTheme.extendedColors.bannerTitle,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//
//                    Spacer(modifier = Modifier.height(5.dp))
//
//                    Text(
//                        text = stringResource(R.string.soft_easy_linen_set),
//                        style = MaterialTheme.typography.titleLarge.copy(
//                            fontWeight = FontWeight.ExtraBold
//                        ),
//                        lineHeight = 24.15.sp,
//                        letterSpacing = (-0.46).sp,
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                }
//
//                Row(
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .background(Color.White)
//                        .clickable { onShopClick() }
//                        .padding(horizontal = 17.dp, vertical = 11.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                ) {
//                    Text(
//                        text = stringResource(R.string.shop),
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF181620)
//                    )
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onBackground,
//                        modifier = Modifier.size(14.dp)
//                    )
//                }
//            }
//        }
//    }
//}
