package com.example.emogrow.features.review.model

import androidx.compose.ui.graphics.Color
import com.example.emogrow.ui.theme.*

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val color: Color,
    val emoji: String,
    val category: String, // e.g., "Giận dữ", "Vui vẻ", etc.
    val content: String = "Đây là nội dung bài học mẫu cho cuốn sách này. Bạn sẽ học được cách nhận biết và điều chỉnh cảm xúc của mình một cách hiệu quả thông qua các hoạt động thú vị."
)

data class ShelfData(
    val date: String,
    val books: List<Book>
)

val sampleBooks = listOf(
    Book("1", "Bài học về tình bạn", "Tác giả: Minh", BookRed, "❤️", "Yêu thương"),
    Book("2", "Cảm xúc của em", "Tác giả: Lan", BookYellow, "😊", "Vui vẻ"),
    Book("3", "Điều kỳ diệu", "Tác giả: Tuấn", BookOrange, "✨", "Vui vẻ"),
    Book("4", "Mặt trời và mây", "Tác giả: Hà", BookGreen, "☀️", "Bình tĩnh"),
    Book("5", "Chú khủng long", "Tác giả: Nam", BookBlue, "🦖", "Buồn"),
    Book("6", "Quả táo đỏ", "Tác giả: Linh", BookPink, "🍎", "Yêu thương"),
    Book("7", "Giấc mơ đẹp", "Tác giả: Anh", BookPurple, "🌙", "Bình tĩnh"),
    Book("8", "Em bé hạnh phúc", "Tác giả: Hoa", BookPink, "👶", "Vui vẻ"),
    Book("9", "Ngày vui", "Tác giả: Đức", BookGreen, "🎉", "Vui vẻ"),
    Book("10", "Kiểm soát cơn giận", "Tác giả: Tâm", BookRed, "🔥", "Tức giận"),
)

val sampleShelves = listOf(
    ShelfData(
        date = "Thứ 2 - 15/05/2024",
        books = sampleBooks.subList(0, 3)
    ),
    ShelfData(
        date = "Thứ 3 - 16/05/2024",
        books = sampleBooks.subList(3, 6)
    ),
    ShelfData(
        date = "Thứ 4 - 17/05/2024",
        books = sampleBooks.subList(6, 9)
    )
)
