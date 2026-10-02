package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FloppaNeeds
import com.example.data.model.FloppaThemeType

@Composable
fun FloppaNeedsWidget(
    needs: FloppaNeeds,
    theme: FloppaThemeType,
    onFeed: () -> Unit,
    onPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hungerProgress by animateFloatAsState(
        targetValue = needs.hungerPercent,
        label = "hungerAnim"
    )

    val affectionProgress by animateFloatAsState(
        targetValue = needs.affectionPercent,
        label = "affectionAnim"
    )

    val isHungry by remember(needs.hunger) {
        derivedStateOf { needs.hunger < 35 }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floppa_needs_widget"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor.copy(alpha = 0.94f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Mood Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐾", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Floppa Care & Moods",
                        color = theme.primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mood Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardColor)
                        .border(1.dp, theme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(needs.moodEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = needs.mood,
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = needs.moodDescription,
                color = theme.textColor.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Need 1: Pelmeni Satiety (Feeding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🥟 Pelmeni Satiety", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${needs.hunger}%", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { hungerProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (isHungry) Color(0xFFEF4444) else theme.primaryColor,
                        trackColor = theme.cardColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onFeed,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("feed_floppa_needs_button")
                ) {
                    Text("Feed 🥟", color = theme.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Need 2: Affection & Pets (Petting)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🐾 Affection & Pets", color = theme.textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${needs.affection}%", color = theme.secondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { affectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (needs.affection < 35) Color(0xFFEF4444) else theme.secondaryColor,
                        trackColor = theme.cardColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onPet,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.cardColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("pet_floppa_needs_button")
                ) {
                    Text("Pet 🐾", color = theme.secondaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
