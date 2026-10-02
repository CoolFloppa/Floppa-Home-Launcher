package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.data.model.FloppaThemeType
import com.example.ui.components.AppItemView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    searchQuery: String,
    selectedCategory: String,
    theme: FloppaThemeType,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onTogglePinToHome: (AppInfo) -> Unit,
    onTogglePinToDock: (AppInfo) -> Unit,
    onCustomizeIcon: (AppInfo) -> Unit,
    onResetIcon: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val categories = remember { listOf("All", "Pinned", "Custom Icons", "System") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Search bar integrated in TopAppBar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text("Search apps...", color = theme.textColor.copy(alpha = 0.5f), fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("app_drawer_search_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.primaryColor)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = theme.textColor)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textColor,
                            unfocusedTextColor = theme.textColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.primaryColor.copy(alpha = 0.25f),
                            focusedContainerColor = theme.cardColor,
                            unfocusedContainerColor = theme.cardColor
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = theme.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.surfaceColor)
            )
        },
        containerColor = theme.backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primaryColor,
                            selectedLabelColor = theme.backgroundColor,
                            containerColor = theme.cardColor,
                            labelColor = theme.textColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = theme.primaryColor.copy(alpha = 0.2f),
                            selectedBorderColor = theme.primaryColor,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Apps Grid
            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😿", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No apps found",
                            color = theme.primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Floppa checked every dumpling box, but nothing matched '$searchQuery'",
                            color = theme.textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 78.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_drawer_grid")
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppItemView(
                            app = app,
                            theme = theme,
                            onClick = { onLaunchApp(app.packageName) },
                            onPinToHome = { onTogglePinToHome(app) },
                            onPinToDock = { onTogglePinToDock(app) },
                            onCustomizeIcon = { onCustomizeIcon(app) },
                            onResetIcon = { onResetIcon(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}
