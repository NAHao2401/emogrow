package com.example.emogrow.features.review.model

data class Sticker(
    val id: String,
    val emoji: String,
    val name: String,
    val relatedBookId: String? = null // Book that unlocks this sticker
)

val sampleStickers = listOf(
    Sticker("1", "☀️", "Mặt trời", "4"),
    Sticker("2", "☁️", "Mây", "4"),
    Sticker("3", "🌧️", "Mưa", "5"),
    Sticker("4", "⚡", "Sét", "10"),
    Sticker("5", "❤️", "Trái tim", "1"),
    Sticker("6", "⭐", "Sao", "2"),
    Sticker("7", "🦖", "Khủng long", "5"),
    Sticker("8", "🎈", "Bóng bay", "2"),
    Sticker("9", "🔺", "Tam giác", null),
    Sticker("10", "🌸", "Hoa", "6"),
    Sticker("11", "🧁", "Cupcake", null),
    Sticker("12", "🦄", "Kỳ lân", null),
)

// All available stickers (including locked ones)
val allStickers = sampleStickers + listOf(
    Sticker("13", "🌈", "Cầu vồng", null),
    Sticker("14", "🎁", "Quà", null),
    Sticker("15", "🎵", "Nhạc", null),
    Sticker("16", "🍦", "Kem", null),
)