package com.example.data.model

data class MemeIconDefinition(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val tag: String
)

object MemeIconPackRepository {
    val builtinIcons = listOf(
        MemeIconDefinition("floppa_cool", "Cool Floppa", "🕶️", "Gosha with stylish sunglasses", "Caracal"),
        MemeIconDefinition("floppa_hefty", "Hefty Gosha", "🐱", "Maximum caracal volume and comfort", "Caracal"),
        MemeIconDefinition("floppa_crown", "King Floppa", "👑", "The royal ruler of the living room", "Royalty"),
        MemeIconDefinition("floppa_hiss", "Spicy Hiss", "😾", "Don't touch my pelmeni", "Mood"),
        MemeIconDefinition("pelmeni_bowl", "Hot Pelmeni", "🥟", "Fresh savory steamed meat dumplings", "Food"),
        MemeIconDefinition("sogga_bro", "Sogga Wildcat", "🐆", "Gosha's loyal serval companion", "Friends"),
        MemeIconDefinition("bingus_frenemy", "Bingus", "🐈‍⬛", "The hairless counterpart", "Friends"),
        MemeIconDefinition("floppa_drip", "Gold Chain Gosha", "💎", "Pure caracal luxury and wealth", "Drip"),
        MemeIconDefinition("floppa_sleep", "Nap Time Flop", "💤", "Exhausted from being too famous", "Rest"),
        MemeIconDefinition("floppa_laser", "Laser Caracal", "🔥", "Overclocked AI computing power", "Cyber"),
        MemeIconDefinition("floppa_chef", "Chef Gosha", "👨‍🍳", "Master of culinary dumplings", "Food"),
        MemeIconDefinition("caracal_tufts", "Holy Ear Tufts", "✨", "The source of all caracal wisdom", "Magic")
    )

    fun findById(id: String?): MemeIconDefinition? {
        return builtinIcons.firstOrNull { it.id == id }
    }
}
