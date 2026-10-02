package com.example.data.model

import android.graphics.drawable.Drawable

enum class CustomIconType {
    NONE,
    BUILTIN_MEME,
    CUSTOM_IMAGE_URI
}

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconDrawable: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isPinnedToHome: Boolean = false,
    val isDockApp: Boolean = false,
    val customIconType: CustomIconType = CustomIconType.NONE,
    val customIconRef: String? = null // Built-in key or image URI string
)
