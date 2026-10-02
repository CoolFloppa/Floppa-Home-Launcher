package com.example.data.model

data class FloppaNeeds(
    val hunger: Int = 85,      // 0 to 100 (Pelmeni Satiety)
    val affection: Int = 80    // 0 to 100 (Petting & Love)
) {
    val hungerPercent: Float
        get() = (hunger.coerceIn(0, 100)) / 100f

    val affectionPercent: Float
        get() = (affection.coerceIn(0, 100)) / 100f

    val mood: String
        get() = when {
            hunger >= 80 && affection >= 80 -> "👑 Royal Bliss"
            hunger >= 55 && affection >= 55 -> "😸 Content King"
            hunger < 40 && affection >= 50 -> "🥟 Hangry Caracal"
            hunger >= 50 && affection < 40 -> "😿 Lonely Floppa"
            hunger < 40 && affection < 40 -> "😾 Spicy Hiss Mode"
            else -> "😺 Cozy Flop"
        }

    val moodDescription: String
        get() = when {
            hunger >= 80 && affection >= 80 -> "Gosha is fully stuffed with pelmeni and basking in glorious ear scratches."
            hunger >= 55 && affection >= 55 -> "Peaceful caracal vibes. Life is good."
            hunger < 40 && affection >= 50 -> "Gosha's stomach is growling! Feed dumplings immediately!"
            hunger >= 50 && affection < 40 -> "Ear tufts are drooping. Please pet Gosha!"
            hunger < 40 && affection < 40 -> "No dumplings AND no pets?! Gosha is preparing a royal hiss."
            else -> "Resting gently on the living room carpet."
        }

    val moodEmoji: String
        get() = when {
            hunger >= 80 && affection >= 80 -> "👑"
            hunger >= 55 && affection >= 55 -> "😸"
            hunger < 40 && affection >= 50 -> "🥟"
            hunger >= 50 && affection < 40 -> "😿"
            hunger < 40 && affection < 40 -> "😾"
            else -> "😺"
        }
}
