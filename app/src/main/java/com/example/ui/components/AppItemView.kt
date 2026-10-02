package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AppInfo
import com.example.data.model.CustomIconType
import com.example.data.model.FloppaThemeType
import com.example.data.model.MemeIconPackRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItemView(
    app: AppInfo,
    theme: FloppaThemeType,
    onClick: () -> Unit,
    onPinToHome: () -> Unit,
    onPinToDock: () -> Unit,
    onCustomizeIcon: () -> Unit,
    onResetIcon: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 54.dp,
    showLabel: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val customImageRequest = remember(app.customIconRef, context) {
        if (app.customIconType == CustomIconType.CUSTOM_IMAGE_URI && !app.customIconRef.isNullOrBlank()) {
            ImageRequest.Builder(context)
                .data(app.customIconRef)
                .crossfade(true)
                .build()
        } else null
    }

    val memeIcon = remember(app.customIconRef, app.customIconType) {
        if (app.customIconType == CustomIconType.BUILTIN_MEME) {
            MemeIconPackRepository.findById(app.customIconRef)
        } else null
    }

    val defaultIconModel = remember(app.packageName, app.iconDrawable) {
        app.iconDrawable
    }

    Box(
        modifier = modifier
            .testTag("app_item_${app.packageName}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surfaceColor.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                when (app.customIconType) {
                    CustomIconType.BUILTIN_MEME -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = memeIcon?.emoji ?: "🐱",
                                fontSize = (iconSize.value * 0.55f).sp
                            )
                        }
                    }

                    CustomIconType.CUSTOM_IMAGE_URI -> {
                        if (customImageRequest != null) {
                            AsyncImage(
                                model = customImageRequest,
                                contentDescription = app.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        } else {
                            Text("🖼️", fontSize = (iconSize.value * 0.5f).sp)
                        }
                    }

                    CustomIconType.NONE -> {
                        if (defaultIconModel != null) {
                            AsyncImage(
                                model = defaultIconModel,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(iconSize * 0.85f)
                            )
                        } else {
                            Text("📱", fontSize = (iconSize.value * 0.5f).sp)
                        }
                    }
                }

                // If customized, small meme badge on bottom right
                if (app.customIconType != CustomIconType.NONE) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(theme.primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 9.sp)
                    }
                }
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.label,
                    color = theme.textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Long-Press Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Open App", fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onClick()
                }
            )

            DropdownMenuItem(
                text = { Text(if (app.isPinnedToHome) "Unpin from Home" else "Pin to Home") },
                leadingIcon = {
                    Icon(
                        if (app.isPinnedToHome) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onPinToHome()
                }
            )

            DropdownMenuItem(
                text = { Text(if (app.isDockApp) "Remove from Dock" else "Add to Dock") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onPinToDock()
                }
            )

            DropdownMenuItem(
                text = { Text("Customize Meme Icon") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onCustomizeIcon()
                }
            )

            if (app.customIconType != CustomIconType.NONE) {
                DropdownMenuItem(
                    text = { Text("Reset to Original Icon", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onResetIcon()
                    }
                )
            }
        }
    }
}
