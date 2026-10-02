package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FloppaThemeType
import kotlinx.coroutines.launch

@Composable
fun FloppaAvatar(
    theme: FloppaThemeType,
    speechText: String,
    onPet: () -> Unit,
    onFeed: () -> Unit,
    onHiss: () -> Unit,
    onOpenAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val earWiggle = remember { Animatable(0f) }
    val tapScale = remember { Animatable(1f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floppa_avatar_surface"),
        shape = RoundedCornerShape(24.dp),
        color = theme.surfaceColor.copy(alpha = 0.92f),
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Floppa Face + Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Floppa Face Canvas
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(tapScale.value)
                        .rotate(earWiggle.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(theme.primaryColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .clickable {
                            coroutineScope.launch {
                                tapScale.animateTo(1.1f, tween(80))
                                earWiggle.animateTo(8f, tween(80))
                                earWiggle.animateTo(-8f, tween(80))
                                earWiggle.animateTo(0f, tween(80))
                                tapScale.animateTo(1f, tween(80))
                            }
                            onPet()
                        }
                        .testTag("floppa_interactive_face"),
                    contentAlignment = Alignment.Center
                ) {
                    FloppaVectorCanvas(theme = theme)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Speech Bubble
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.cardColor)
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Big Floppa (Gosha)",
                            color = theme.primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Powered",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = speechText,
                        color = theme.textColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row: Feed Pelmeni, Pet, Hiss, Ask AI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFeed,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("feed_dumpling_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Text("🥟 Feed +10", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.backgroundColor)
                }

                Button(
                    onClick = onPet,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("pet_floppa_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Text("😻 Pet", fontSize = 12.sp, color = theme.textColor)
                }

                Button(
                    onClick = onHiss,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("hiss_floppa_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Text("😾 Hiss", fontSize = 12.sp, color = theme.textColor)
                }

                Button(
                    onClick = onOpenAi,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("ask_floppa_ai_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.secondaryColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                ) {
                    Text("✨ Ask AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.backgroundColor)
                }
            }
        }
    }
}

@Composable
fun FloppaVectorCanvas(
    theme: FloppaThemeType,
    modifier: Modifier = Modifier.size(80.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Left ear (Caracal triangle with black tuft)
        val leftEar = Path().apply {
            moveTo(w * 0.28f, h * 0.45f)
            lineTo(w * 0.22f, h * 0.16f)
            lineTo(w * 0.44f, h * 0.35f)
            close()
        }
        drawPath(leftEar, color = theme.primaryColor)

        // Left Ear Black Tufts
        val leftTufts = Path().apply {
            moveTo(w * 0.22f, h * 0.16f)
            lineTo(w * 0.16f, h * 0.05f)
            lineTo(w * 0.20f, h * 0.14f)
            lineTo(w * 0.19f, h * 0.03f)
            lineTo(w * 0.25f, h * 0.13f)
            lineTo(w * 0.28f, h * 0.06f)
            lineTo(w * 0.26f, h * 0.18f)
            close()
        }
        drawPath(leftTufts, color = theme.floppaEarColor)

        // Right ear
        val rightEar = Path().apply {
            moveTo(w * 0.72f, h * 0.45f)
            lineTo(w * 0.78f, h * 0.16f)
            lineTo(w * 0.56f, h * 0.35f)
            close()
        }
        drawPath(rightEar, color = theme.primaryColor)

        // Right Ear Black Tufts
        val rightTufts = Path().apply {
            moveTo(w * 0.78f, h * 0.16f)
            lineTo(w * 0.84f, h * 0.05f)
            lineTo(w * 0.80f, h * 0.14f)
            lineTo(w * 0.81f, h * 0.03f)
            lineTo(w * 0.75f, h * 0.13f)
            lineTo(w * 0.72f, h * 0.06f)
            lineTo(w * 0.74f, h * 0.18f)
            close()
        }
        drawPath(rightTufts, color = theme.floppaEarColor)

        // Floppa Face Oval
        drawOval(
            color = theme.primaryColor,
            topLeft = Offset(w * 0.22f, h * 0.34f),
            size = Size(w * 0.56f, h * 0.54f)
        )

        // Cheeks (Hefty Floppa Volume)
        drawOval(
            color = theme.secondaryColor,
            topLeft = Offset(w * 0.18f, h * 0.46f),
            size = Size(w * 0.64f, h * 0.42f)
        )

        // Light Muzzle
        drawOval(
            color = Color(0xFFFEF3C7),
            topLeft = Offset(w * 0.36f, h * 0.58f),
            size = Size(w * 0.28f, h * 0.26f)
        )

        // Nose
        val nose = Path().apply {
            moveTo(w * 0.45f, h * 0.63f)
            lineTo(w * 0.55f, h * 0.63f)
            lineTo(w * 0.50f, h * 0.69f)
            close()
        }
        drawPath(nose, color = Color(0xFF78350F))

        // Sunglasses (Meme Floppa Sunglasses)
        // Left glass
        drawRoundRect(
            color = Color(0xFF111827),
            topLeft = Offset(w * 0.27f, h * 0.46f),
            size = Size(w * 0.20f, h * 0.14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Right glass
        drawRoundRect(
            color = Color(0xFF111827),
            topLeft = Offset(w * 0.53f, h * 0.46f),
            size = Size(w * 0.20f, h * 0.14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Bridge
        drawLine(
            color = Color(0xFF111827),
            start = Offset(w * 0.46f, h * 0.50f),
            end = Offset(w * 0.54f, h * 0.50f),
            strokeWidth = 4f
        )
        // Glare shine
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.30f, h * 0.49f),
            end = Offset(w * 0.36f, h * 0.49f),
            strokeWidth = 2.5f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.56f, h * 0.49f),
            end = Offset(w * 0.62f, h * 0.49f),
            strokeWidth = 2.5f
        )

        // Whiskers
        drawLine(
            color = Color(0xFFFFFBEB),
            start = Offset(w * 0.35f, h * 0.68f),
            end = Offset(w * 0.14f, h * 0.66f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFFFFFBEB),
            start = Offset(w * 0.35f, h * 0.72f),
            end = Offset(w * 0.15f, h * 0.74f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFFFFFBEB),
            start = Offset(w * 0.65f, h * 0.68f),
            end = Offset(w * 0.86f, h * 0.66f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFFFFFBEB),
            start = Offset(w * 0.65f, h * 0.72f),
            end = Offset(w * 0.85f, h * 0.74f),
            strokeWidth = 1.5f
        )
    }
}
