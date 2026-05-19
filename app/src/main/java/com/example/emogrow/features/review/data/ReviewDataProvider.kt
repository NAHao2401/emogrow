package com.example.emogrow.features.review.data

import androidx.compose.ui.graphics.Color
import com.example.emogrow.features.review.model.EmotionDiary
import com.example.emogrow.features.review.model.EmotionEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ReviewDataProvider {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val allEmotions: List<EmotionEntry> = listOf(
        EmotionEntry("vui-ve", "Vui vẻ", "Cảm xúc khi trẻ cảm thấy hạnh phúc, thoải mái và muốn cười.", "#FFD54F", "😊"),
        EmotionEntry("buon", "Buồn", "Cảm xúc khi trẻ cảm thấy không vui, thất vọng hoặc mất mát.", "#64B5F6", "😢"),
        EmotionEntry("tuc-gian", "Tức giận", "Cảm xúc khi trẻ cảm thấy khó chịu, bực bội hoặc không hài lòng.", "#EF5350", "😡"),
        EmotionEntry("so-hai", "Sợ hãi", "Cảm xúc khi trẻ cảm thấy lo lắng, bất an hoặc sợ một điều gì đó.", "#9575CD", "😨"),
        EmotionEntry("ngac-nhien", "Ngạc nhiên", "Cảm xúc khi trẻ gặp điều bất ngờ hoặc chưa từng nghĩ tới.", "#FFB74D", "😮"),
        EmotionEntry("lo-lang", "Lo lắng", "Cảm xúc khi trẻ cảm thấy bồn chồn, hồi hộp hoặc không yên tâm.", "#4DB6AC", "😟"),
        EmotionEntry("xau-ho", "Xấu hổ", "Cảm xúc khi trẻ cảm thấy ngại ngùng, lúng túng hoặc mắc cỡ.", "#F48FB1", "😳"),
        EmotionEntry("tu-hao", "Tự hào", "Cảm xúc khi trẻ cảm thấy vui vì đã làm được điều tốt hoặc đạt thành quả.", "#81C784", "😊"),
        EmotionEntry("yeu-thuong", "Yêu thương", "Cảm xúc khi trẻ cảm thấy được quan tâm, gần gũi hoặc muốn thể hiện tình cảm.", "#F06292", "🥰"),
        EmotionEntry("binh-tinh", "Bình tĩnh", "Cảm xúc khi trẻ cảm thấy thoải mái, nhẹ nhàng và không căng thẳng.", "#90CAF9", "😌"),
        EmotionEntry("met-moi", "Mệt mỏi", "Cảm xúc khi trẻ cảm thấy thiếu năng lượng, buồn ngủ hoặc cần nghỉ ngơi.", "#B0BEC5", "😴"),
        EmotionEntry("co-don", "Cô đơn", "Cảm xúc khi trẻ cảm thấy một mình, thiếu sự chia sẻ hoặc cần được quan tâm.", "#A1887F", "🥺"),
        EmotionEntry("boi-roi", "Bối rối", "Cảm xúc khi trẻ chưa hiểu rõ điều gì đó hoặc không biết nên làm gì.", "#CE93D8", "😕"),
        EmotionEntry("ghen-ti", "Ghen tị", "Cảm xúc khi trẻ cảm thấy không vui vì người khác có điều mình mong muốn.", "#AED581", "😒"),
        EmotionEntry("hao-hung", "Hào hứng", "Cảm xúc khi trẻ cảm thấy rất vui, mong chờ hoặc thích thú với điều gì đó.", "#FF8A65", "🤩")
    )

    fun getEmotions(): List<EmotionEntry> = allEmotions

    fun getEmotionById(id: String): EmotionEntry? = allEmotions.find { it.emotionId == id }

    fun getDiaries(childId: Int, viewDate: LocalDate = LocalDate.now()): List<EmotionDiary> {
        val today = LocalDate.now()
        // If viewing current month: only show days up to today
        // If viewing past month: show all days
        val isCurrentMonth = viewDate.year == today.year && viewDate.monthValue == today.monthValue
        val maxDay = if (isCurrentMonth) today.dayOfMonth else viewDate.lengthOfMonth()

        return (1..maxDay).map { day ->
            val date = viewDate.withDayOfMonth(day)
            val emotion = allEmotions[(day - 1) % allEmotions.size]
            EmotionDiary(
                diaryId = "d-${childId}-${date.format(dateFormatter)}",
                childId = childId,
                emotionId = emotion.emotionId,
                diaryDate = date.format(dateFormatter),
                seedColor = emotion.colorCode,
                feelingNote = getFeelingNote(emotion.name, date.format(displayFormatter))
            )
        }
    }

    fun getCurrentWeekDiaries(childId: Int): List<EmotionDiary> {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        return (0..6).map { dayOffset ->
            val date = monday.plusDays(dayOffset.toLong())
            val emotion = allEmotions[(dayOffset) % allEmotions.size]
            EmotionDiary(
                diaryId = "w-${childId}-${date.format(dateFormatter)}",
                childId = childId,
                emotionId = emotion.emotionId,
                diaryDate = date.format(dateFormatter),
                seedColor = emotion.colorCode,
                feelingNote = getFeelingNote(emotion.name, date.format(displayFormatter))
            )
        }
    }

    fun getBeadsForDiaries(diaries: List<EmotionDiary>): List<DiaryBead> {
        return diaries.map { diary ->
            val emotion = getEmotionById(diary.emotionId)
            DiaryBead(
                id = diary.diaryId,
                date = diary.diaryDate,
                displayDate = LocalDate.parse(diary.diaryDate, dateFormatter).format(displayFormatter),
                emotionId = diary.emotionId,
                emoji = emotion?.emoji ?: "😊",
                color = parseColor(emotion?.colorCode ?: "#FFD54F"),
                label = emotion?.name ?: "Unknown",
                description = diary.feelingNote,
                isToday = diary.diaryDate == LocalDate.now().format(dateFormatter)
            )
        }
    }

    private fun getFeelingNote(emotionName: String, dateDisplay: String): String {
        return when (emotionName) {
            "Vui vẻ" -> "Ngày $dateDisplay con rất vui vì được đi chơi công viên cùng gia đình."
            "Buồn" -> "Ngày $dateDisplay con buồn vì trời mưa không được ra ngoài chơi."
            "Tức giận" -> "Ngày $dateDisplay con hơi bực vì đồ chơi yêu thích bị hỏng."
            "Sợ hãi" -> "Ngày $dateDisplay con cảm thấy hơi sợ khi xem phim hoạt hình."
            "Ngạc nhiên" -> "Ngày $dateDisplay con ngạc nhiên khi được tặng quà bất ngờ."
            "Lo lắng" -> "Ngày $dateDisplay con lo lắng vì bài kiểm tra sắp tới."
            "Xấu hổ" -> "Ngày $dateDisplay con ngại khi phải nói trước lớp."
            "Tự hào" -> "Ngày $dateDisplay con tự hào vì được điểm tốt trong bài tập."
            "Yêu thương" -> "Ngày $dateDisplay con cảm thấy được yêu thương khi bố mẹ ôm."
            "Bình tĩnh" -> "Ngày $dateDisplay con cảm thấy bình lặng khi nghe nhạc êm dịu."
            "Mệt mỏi" -> "Ngày $dateDisplay con mệt sau một ngày học nhiều."
            "Cô đơn" -> "Ngày $dateDisplay con cảm thấy cô đơn vì bạn bè không có nhà."
            "Bối rối" -> "Ngày $dateDisplay con bối rối vì chưa hiểu bài toán mới."
            "Ghen tị" -> "Ngày $dateDisplay con hơi ghen khi thấy bạn có đồ chơi mới."
            "Hào hứng" -> "Ngày $dateDisplay con hào hứng vì sắp có sinh nhật."
            else -> "Ngày $dateDisplay con ghi nhận cảm xúc của mình."
        }
    }

    private fun parseColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFFFFD54F)
        }
    }
}

data class DiaryBead(
    val id: String,
    val date: String,
    val displayDate: String,
    val emotionId: String,
    val emoji: String,
    val color: Color,
    val label: String,
    val description: String,
    val isToday: Boolean = false
)