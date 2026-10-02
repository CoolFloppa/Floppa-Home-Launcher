package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class FloppaThemeType(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardColor: Color,
    val textColor: Color,
    val floppaEarColor: Color,
    val quote: String
) {
    CLASSIC_GOSHA(
        title = "Classic Gosha",
        subtitle = "Royal caracal amber & dark ear tufts",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFD97706),
        tertiaryColor = Color(0xFFB45309),
        backgroundColor = Color(0xFF18130E),
        surfaceColor = Color(0xFF261D15),
        cardColor = Color(0xFF33261C),
        textColor = Color(0xFFFEF3C7),
        floppaEarColor = Color(0xFF0F0D0B),
        quote = "Flop for no hoe. Keep the caracal grind."
    ),
    DUMPLING_FEAST(
        title = "Dumpling Feast",
        subtitle = "Steamy pelmeni gold & rich sour cream",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFFFCD34D),
        tertiaryColor = Color(0xFF10B981),
        backgroundColor = Color(0xFF1A1713),
        surfaceColor = Color(0xFF2B251D),
        cardColor = Color(0xFF3D3428),
        textColor = Color(0xFFFFFBEB),
        floppaEarColor = Color(0xFF1C1917),
        quote = "Pelmeni on my plate, peace in my mind."
    ),
    SOGGA_SWAMP(
        title = "Sogga Savannah",
        subtitle = "Wildcat emerald moss & warm gold",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        tertiaryColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF0F1A15),
        surfaceColor = Color(0xFF172920),
        cardColor = Color(0xFF1E362B),
        textColor = Color(0xFFECFDF5),
        floppaEarColor = Color(0xFF064E3B),
        quote = "Sogga and Floppa run this device."
    ),
    CYBER_FLOPPA(
        title = "Cyber Floppa 2077",
        subtitle = "Neon cyan & synthwave caracal magenta",
        primaryColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFFEC4899),
        tertiaryColor = Color(0xFF8B5CF6),
        backgroundColor = Color(0xFF0B0E17),
        surfaceColor = Color(0xFF14192B),
        cardColor = Color(0xFF1E243D),
        textColor = Color(0xFFE0F2FE),
        floppaEarColor = Color(0xFF080B12),
        quote = "Wake up Samurai, we have dumplings to eat."
    ),
    MIDNIGHT_FLOP(
        title = "Midnight Flop",
        subtitle = "Pure AMOLED pitch black with regal gold",
        primaryColor = Color(0xFFFFD700),
        secondaryColor = Color(0xFFE5A91E),
        tertiaryColor = Color(0xFF60A5FA),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF121212),
        cardColor = Color(0xFF1C1C1E),
        textColor = Color(0xFFFFFFFF),
        floppaEarColor = Color(0xFF050505),
        quote = "Peak battery saving, maximum caracal heft."
    );

    companion object {
        fun fromName(name: String?): FloppaThemeType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CLASSIC_GOSHA
        }
    }
}
