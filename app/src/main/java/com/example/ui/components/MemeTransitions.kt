package com.example.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.example.data.model.MemeTransitionType
import com.example.ui.LauncherScreen

object MemeTransitions {

    fun getTransitionSpec(
        type: MemeTransitionType
    ): AnimatedContentTransitionScope<LauncherScreen>.() -> ContentTransform {
        return {
            when (type) {
                MemeTransitionType.FLOP_FLIP -> {
                    // Snappy responsive scale fade
                    (scaleIn(initialScale = 0.95f, animationSpec = tween(120, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(100))) togetherWith
                        (scaleOut(targetScale = 1.05f, animationSpec = tween(100)) + fadeOut(animationSpec = tween(90)))
                }

                MemeTransitionType.DUMPLING_BOUNCE -> {
                    // Quick crisp bounce
                    (scaleIn(initialScale = 0.92f, animationSpec = tween(120)) + fadeIn(animationSpec = tween(100))) togetherWith
                        (scaleOut(targetScale = 0.96f, animationSpec = tween(90)) + fadeOut(animationSpec = tween(80)))
                }

                MemeTransitionType.EAR_TUFT_SLIDE -> {
                    // Crisp slide
                    val isMovingForward = targetState.ordinal > initialState.ordinal
                    if (isMovingForward) {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = tween(130, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(100))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                animationSpec = tween(100)
                            ) + fadeOut(animationSpec = tween(90)))
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 2 },
                            animationSpec = tween(130, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(100))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth / 4 },
                                animationSpec = tween(100)
                            ) + fadeOut(animationSpec = tween(90)))
                    }
                }

                MemeTransitionType.SOGGA_ZOOM -> {
                    // Snappy vertical slide
                    (slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 6 },
                        animationSpec = tween(120, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(100))) togetherWith
                        (slideOutVertically(
                            targetOffsetY = { fullHeight -> -fullHeight / 8 },
                            animationSpec = tween(100)
                        ) + fadeOut(animationSpec = tween(80)))
                }

                MemeTransitionType.CYBER_GLITCH -> {
                    // Instant cut
                    (scaleIn(initialScale = 0.99f, animationSpec = tween(60)) + fadeIn(animationSpec = tween(60))) togetherWith
                        (scaleOut(targetScale = 1.01f, animationSpec = tween(60)) + fadeOut(animationSpec = tween(60)))
                }
            }
        }
    }
}
