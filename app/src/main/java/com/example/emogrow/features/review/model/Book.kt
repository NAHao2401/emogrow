package com.example.emogrow.features.review.model

import androidx.compose.ui.graphics.Color
import com.example.emogrow.ui.theme.*

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val color: Color,
    val emoji: String,
    val category: String, // Khớp với label trong ReviewDataProvider
    val content: String = "Đây là nội dung bài học mẫu cho cuốn sách này. Bạn sẽ học được cách nhận biết và điều chỉnh cảm xúc của mình một cách hiệu quả thông qua các hoạt động thú vị."
)

data class ShelfData(
    val id: String,
    val date: String,
    val books: List<Book>
)

val sampleBooks = listOf(
    // Vui vẻ
    Book("1", "Bí kíp nụ cười", "Tác giả: Minh", BookYellow, "😊", "Vui vẻ"),
    Book("2", "Niềm vui mỗi ngày", "Tác giả: Lan", BookYellow, "✨", "Vui vẻ"),
    // Buồn
    Book("3", "Khi nỗi buồn ghé thăm", "Tác giả: Tuấn", BookBlue, "😢", "Buồn"),
    Book("4", "Vượt qua thất vọng", "Tác giả: Hà", BookBlue, "🌈", "Buồn"),
    // Tức giận
    Book("5", "Kiểm soát cơn giận", "Tác giả: Tâm", BookRed, "🔥", "Tức giận"),
    Book("6", "Hơi thở bình tĩnh", "Tác giả: Nam", BookRed, "🌬️", "Tức giận"),
    // Sợ hãi
    Book("7", "Dũng cảm lên nào", "Tác giả: Linh", BookPurple, "🛡️", "Sợ hãi"),
    Book("8", "Bóng tối đáng yêu", "Tác giả: Anh", BookPurple, "🌙", "Sợ hãi"),
    // Ngạc nhiên
    Book("9", "Thế giới bất ngờ", "Tác giả: Đức", BookOrange, "😮", "Ngạc nhiên"),
    // Lo lắng
    Book("10", "Gói ghém lo âu", "Tác giả: Hoa", BookGreen, "📦", "Lo lắng"),
    // Xấu hổ
    Book("11", "Tự tin là chính mình", "Tác giả: Bảo", BookPink, "💖", "Xấu hổ"),
    // Tự hào
    Book("12", "Tớ đã làm được!", "Tác giả: Việt", BookGreen, "🏆", "Tự hào"),
    // Yêu thương
    Book("13", "Vòng tay ấm áp", "Tác giả: Mai", BookPink, "🥰", "Yêu thương"),
    Book("14", "Lời yêu thương", "Tác giả: Vy", BookPink, "❤️", "Yêu thương"),
    // Bình tĩnh
    Book("15", "Tâm hồn thư thái", "Tác giả: Khoa", BookBlue, "😌", "Bình tĩnh"),
    Book("16", "Mặt hồ phẳng lặng", "Tác giả: An", BookBlue, "💧", "Bình tĩnh"),
    // Mệt mỏi
    Book("17", "Nghỉ ngơi một chút", "Tác giả: Hạnh", BookOrange, "😴", "Mệt mỏi"),
    // Cô đơn
    Book("18", "Luôn có bạn bên cạnh", "Tác giả: Phúc", BookPurple, "🤝", "Cô đơn"),
    // Bối rối
    Book("19", "Gỡ rối tơ lòng", "Tác giả: Chi", BookOrange, "😕", "Bối rối"),
    // Ghen tị
    Book("20", "Chia sẻ niềm vui", "Tác giả: Thảo", BookGreen, "🤝", "Ghen tị"),
    // Hào hứng
    Book("21", "Cuộc phiêu lưu mới", "Tác giả: Quang", BookOrange, "🤩", "Hào hứng")
)

val sampleShelves = listOf(
    ShelfData(
        id = "sample-1",
        date = "Thứ 2 - 15/05/2024",
        books = sampleBooks.subList(0, 3)
    ),
    ShelfData(
        id = "sample-2",
        date = "Thứ 3 - 16/05/2024",
        books = sampleBooks.subList(3, 6)
    ),
    ShelfData(
        id = "sample-3",
        date = "Thứ 4 - 17/05/2024",
        books = sampleBooks.subList(6, 9)
    )
)
