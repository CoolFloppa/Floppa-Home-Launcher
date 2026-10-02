package com.example.data.model

enum class MemeTransitionType(
    val title: String,
    val description: String,
    val iconEmoji: String
) {
    FLOP_FLIP(
        title = "Flop 3D Flip",
        description = "Gosha rolls over on the rug in glorious 3D",
        iconEmoji = "🔄"
    ),
    DUMPLING_BOUNCE(
        title = "Dumpling Bounce",
        description = "Plump and juicy pelmeni spring animation",
        iconEmoji = "🥟"
    ),
    EAR_TUFT_SLIDE(
        title = "Ear Tuft Slide",
        description = "Sharp angular slide with caracal tuft tilt",
        iconEmoji = "🐱"
    ),
    SOGGA_ZOOM(
        title = "Sogga Wild Zoom",
        description = "Dramatic cinematic zoom in and out",
        iconEmoji = "🐆"
    ),
    CYBER_GLITCH(
        title = "Cyber Caracal Glitch",
        description = "High-tech synthwave instant meme snap",
        iconEmoji = "⚡"
    );

    companion object {
        fun fromName(name: String?): MemeTransitionType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FLOP_FLIP
        }
    }
}
