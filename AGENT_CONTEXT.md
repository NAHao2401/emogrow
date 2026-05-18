# 🎮 Agent Context — Màn hình Trò chơi Ghép mặt Cảm xúc

> **Đọc file này trước khi làm bất kỳ task nào liên quan đến GameScreen.**
> File này mô tả toàn bộ context, kiến trúc, design system và quy tắc của màn hình trò chơi.

---

## 1. Tổng quan dự án

**Tên app:** Ứng dụng học cảm xúc cho trẻ em (đặc biệt trẻ khó nhận biết cảm xúc)
**Tech stack:** Kotlin + Jetpack Compose (không dùng XML layout)
**Target users:** Trẻ em 4–8 tuổi, có thể chưa biết đọc
**Ngôn ngữ UI:** Tiếng Việt hoàn toàn

### 4 màn hình của app:
| Màn hình | Route | Mô tả | Owner |
|----------|-------|-------|-------|
| Học cảm xúc | `onNavigateToLesson` | Flashcard học cảm xúc | Team |
| **Trò chơi** | `onNavigateToGame` | Ghép khuôn mặt drag & drop | **Người 1 (tôi)** |
| Nhật ký | `onNavigateToJournal` | Gieo hạt cảm xúc hàng ngày | Team |
| Xem lại | `onNavigateToReview` | Nhìn lại hành trình cảm xúc | Team |

---

## 2. Cấu trúc file — GameScreen

```
ui/game/
├── GameScreen.kt         ← Scaffold chính, drag overlay, LaunchedEffect
├── FaceCanvas.kt         ← Oval mặt trống + 4 drop zones
├── PartsTray.kt          ← Khay cuộn ngang bên dưới
├── DraggablePart.kt      ← Card bộ phận có thể kéo thả
├── FacePartDrawing.kt    ← Vẽ bộ phận bằng Canvas (KHÔNG dùng emoji)
├── GameViewModel.kt      ← State management + validation logic
├── GameTypes.kt          ← Data types + GameDesign object
└── GameDesign.kt         ← Design system (màu, shape, spacing)
```

**Quy tắc:** Không được thêm file ngoài danh sách trên mà không hỏi trước.

---

## 3. Data Types (GameTypes.kt) — KHÔNG được sửa

```kotlin
enum class EmotionType { HAPPY, SAD, ANGRY, SURPRISED, SCARED, WORRIED, SHY, PROUD }
enum class ZoneId {
    LEFT_EYE, RIGHT_EYE,
    LEFT_EYEBROW, RIGHT_EYEBROW,
    LEFT_CHEEK, RIGHT_CHEEK,
    NOSE, MOUTH,
    SWEAT
}
enum class PartType { EYE, NOSE, MOUTH, EYEBROW, CHEEK, SWEAT }

data class FacePart(
    val id: String,           // "eye_happy", "mouth_smile", v.v.
    val type: PartType,
    val emotion: EmotionType?,
    val emoji: String,        // giữ lại cho backward compat, KHÔNG render ra UI
    val label: String         // tiếng Việt: "Mắt vui", "Miệng cười"
)

data class DropZone(
    val id: ZoneId,
    val accepts: PartType,
    val offsetX: Float,
    val offsetY: Float
)

data class GameRound(
    val emotion: EmotionType,
    val promptText: String,
    val promptEmoji: String,
    val targetFace: Map<ZoneId, String>,   // zoneId → partId
    val availableParts: List<FacePart>,
    val isReview: Boolean
)

data class GameUiState(
    val currentRound: GameRound,
    val placedParts: Map<ZoneId, FacePart?> = ZoneId.values().associateWith { null },
    val isCompleted: Boolean = false,
    val draggedPart: FacePart? = null,
    val dragPosition: Offset = Offset.Zero
)
```

---

## 4. GameViewModel — Logic rules

### State flow chuẩn:
```
startDrag() → updateDragPosition() → tryDropPart()
    ↓ (nếu drop đúng zone)
placedParts updated → checkCompletion()
    ↓ (nếu tất cả đúng)
isCompleted = true → onFaceCompleted() callback
    ↓ (sau 2500ms delay ở UI)
goToNextRound()
```

### Các hàm BẮT BUỘC phải có:
```kotlin
fun startDrag(part: FacePart)
fun updateDragPosition(offset: Offset)
fun tryDropPart(dropPosition: Offset, faceCanvasPosition: Offset, faceSize: Size)
fun checkCompletion()   // private — gọi sau mỗi tryDropPart
fun goToNextRound()     // public — GameScreen gọi sau delay
fun removePart(zoneId: ZoneId)  // tap part đã đặt để bỏ ra
```

### Quy tắc checkCompletion:
```kotlin
// ĐÚNG — so sánh bằng .id string, không phải object reference:
val allCorrect = ZoneId.values().all { zoneId ->
    placedParts[zoneId]?.id == currentRound.targetFace[zoneId]
}
// SAI — không dùng == trực tiếp trên FacePart object
```

### Quy tắc goToNextRound:
- Dùng `currentRoundIndex` private var
- `(currentRoundIndex + 1) % sampleRounds.size` — vòng lặp vô hạn
- Reset hoàn toàn: `placedParts`, `isCompleted`, `draggedPart`, `dragPosition`

---

## 5. Interface với Người 2 (teammate) — KHÔNG được thay đổi

```kotlin
// GameScreen nhận vào:
onFaceCompleted: (emotion: EmotionType, isReview: Boolean) -> Unit
// → Người 2 implement: bắn confetti + animation nhãn dán bay vào Album

onCelebrationDone: () -> Unit
// → Người 2 gọi khi animation xong
// → GameScreen dùng để biết khi nào gọi goToNextRound()

// Thay vì delay cứng 2500ms, ưu tiên dùng onCelebrationDone nếu Người 2 đã implement
```

---

## 6. FaceCanvas — Vị trí drop zones (face size = 260.dp)

```
Tọa độ offset tính từ CENTER của hình tròn mặt:

LEFT_EYE:   x = -48.dp,  y = -72.dp   | size = 64 × 56.dp
RIGHT_EYE:  x = +48.dp,  y = -72.dp   | size = 64 × 56.dp
LEFT_EYEBROW:  x = -48.dp,  y = -100.dp  | size = 60 × 28.dp
RIGHT_EYEBROW: x = +48.dp,  y = -100.dp  | size = 60 × 28.dp
LEFT_CHEEK:    x = -80.dp,  y = +20.dp   | size = 52 × 40.dp
RIGHT_CHEEK:   x = +80.dp,  y = +20.dp   | size = 52 × 40.dp
NOSE:       x =   0.dp,  y =  +5.dp   | size = 48 × 44.dp
MOUTH:      x =   0.dp,  y = +70.dp   | size = 80 × 48.dp
SWEAT:       x = +72.dp,  y =  +5.dp  | size = 28 × 40.dp
```

### Drop zone visual states:
| State | Visual |
|-------|--------|
| Empty | Dashed border, `PathEffect.dashPathEffect(floatArrayOf(8f,6f), 0f)` |
| Hover (correct type) | Pulsing glow, scale 1.0→1.1 loop animation |
| Hover (wrong type) | Red tint border — cảnh báo |
| Filled | Hiển thị `FacePartDrawing`, bounce-in animation (scale 0→1, spring) |

### Snap tolerance: 80.dp (tính từ center của zone)

---

## 7. FacePartDrawing — PNG rendering (đã cập nhật)

Dùng `Image(painterResource(...))` thay vì Canvas.
`isMirrored` param vẫn giữ nhưng logic xử lý bên trong:

- Eye trái/phải dùng file PNG riêng biệt (không cần graphicsLayer flip)
- Tray luôn dùng file LEFT (`isMirrored = false`)
- Canvas dùng đúng LEFT/RIGHT theo ZoneId

File mapping:

- `eye_happy` + `false` → `part_eye_happy_left`
- `eye_happy` + `true` → `part_eye_happy_right`
- `eye_sad` + `false` → `part_eye_sad_left`
- `eye_sad` + `true` → `part_eye_sad_right`
- `eye_angry` + `false` → `part_eye_angry_left`
- `eye_angry` + `true` → `part_eye_angry_right`
- `nose_basic` → `part_nose_normal`
- `mouth_smile` → `part_mouth_happy`
- `mouth_frown` → `part_mouth_sad`
- `mouth_angry` → `part_mouth_angry`
 - `eye_surprised_left` → `part_eye_surprised_left`
 - `eye_surprised_right` → `part_eye_surprised_right`
 - `eye_worried_left` → `part_eye_worried_left`
 - `eye_worried_right` → `part_eye_worried_right`
 - `eye_shy_left` → `part_eye_shy_left`
 - `eye_shy_right` → `part_eye_shy_right`
 - `eye_proud` → `part_eye_proud`
 - `eyebrows_scared_left` → `part_eyebrows_scared_left`
 - `eyebrows_scared_right` → `part_eyebrows_scared_right`
 - `eyebrows_shy_left` → `part_eyebrows_shy_left`
 - `eyebrows_shy_right` → `part_eyebrows_shy_right`
 - `mouth_surprised` → `part_mouth_surprised`
 - `mouth_worried` → `part_mouth_worried`
 - `mouth_shy` → `part_mouth_shy`
 - `mouth_proud` → `part_mouth_proud`
 - `mouth_scared` → `part_mouth_scared`
 - `blush_shy` → `part_blush_shy`
 - `sweat` → `part_sweat`

### Kích thước render:
| Vị trí | Size |
|--------|------|
| Trong PartsTray card | 60.dp |
| Trên FaceCanvas (eye) | 56.dp |
| Trên FaceCanvas (nose) | 42.dp |
| Trên FaceCanvas (mouth) | 72.dp |
| Trên FaceCanvas (eyebrow) | 52.dp |
| Trên FaceCanvas (cheek) | 44.dp |
| Trên FaceCanvas (sweat) | 24.dp |
| Drag overlay | 64.dp + scale 1.15f |

---

## 8. Design System (GameDesign.kt)

```kotlin
object GameDesign {
    // Backgrounds
    val screenBg       = Color(0xFFFFF9F0)
    val cardBg         = Color(0xFFFFFFFF)
    val faceBg         = Color(0xFFFFF8EC)

    // Emotion gradients
    val happyGradient     = listOf(Color(0xFFFFD93D), Color(0xFFFF9500))
    val sadGradient       = listOf(Color(0xFF74B9FF), Color(0xFF0984E3))
    val angryGradient     = listOf(Color(0xFFFF7675), Color(0xFFD63031))
    val surprisedGradient = listOf(Color(0xFFA29BFE), Color(0xFF6C5CE7))
    val scaredGradient    = listOf(Color(0xFF55EFC4), Color(0xFF00B894))

    // Emotion card tints (for part cards in tray)
    val happyTint     = Color(0xFFFFF9E0)
    val sadTint       = Color(0xFFE8F4FF)
    val angryTint     = Color(0xFFFFEEEE)
    val neutralTint   = Color(0xFFF5ECD7)  // nose

    // Emotion border colors (darker version of tint)
    val happyBorder   = Color(0xFFFFD93D)
    val sadBorder     = Color(0xFF74B9FF)
    val angryBorder   = Color(0xFFFF7675)
    val neutralBorder = Color(0xFFE8C97A)

    // Text
    val textDark   = Color(0xFF2D1B0E)
    val textMid    = Color(0xFF7B5E3A)
    val faceGold   = Color(0xFFE8C97A)

    // Spacing & Shape
    val screenPaddingH = 20.dp
    val headerRadius   = 28.dp
    val trayRadius     = 36.dp
    val partCardRadius = 20.dp
    val faceSize       = 260.dp

    // Typography
    val promptFontSize = 26.sp
    val labelFontSize  = 11.sp
    val trayTitleSize  = 14.sp

    fun emotionGradient(emotion: EmotionType) = when(emotion) {
        EmotionType.HAPPY     -> happyGradient
        EmotionType.SAD       -> sadGradient
        EmotionType.ANGRY     -> angryGradient
        EmotionType.SURPRISED -> surprisedGradient
        EmotionType.SCARED    -> scaredGradient
    }

    fun partCardTint(emotion: EmotionType?) = when(emotion) {
        EmotionType.HAPPY  -> happyTint
        EmotionType.SAD    -> sadTint
        EmotionType.ANGRY  -> angryTint
        null               -> neutralTint
        else               -> neutralTint
    }

    fun partCardBorder(emotion: EmotionType?) = when(emotion) {
        EmotionType.HAPPY  -> happyBorder
        EmotionType.SAD    -> sadBorder
        EmotionType.ANGRY  -> angryBorder
        null               -> neutralBorder
        else               -> neutralBorder
    }
}
```

### 5 nguyên tắc UI bắt buộc:
1. **Bold outlines** — mọi card có 2.dp colored border, không dùng gray
2. **3D depth** — mọi element tương tác có offset shadow box bên dưới (như Duolingo)
3. **No flat white** — card luôn có emotion tint, không bao giờ pure white
4. **Large touch targets** — minimum 88.dp cho mọi draggable element
5. **High contrast** — text `#2D1B0E` trên nền sáng, White trên gradient

---

## 14. Bug Fixes Log (2026-05-04) — 3 lỗi đã fix

### Bug 1 — Khay bộ phận không cuộn được khi hàng trên thừa bộ phận ✅

**Vấn đề:** Khi availableParts có số lượng lẻ, hàng 1 có nhiều phần tử hơn hàng 2, khiến cột cuối có card trống ở hàng dưới. Gesture của drag & drop có thể consume scroll event.

**Fix áp dụng:** 
- Giữ nguyên kiến trúc 2-row synchronized scroll (đã verify hoạt động tốt)
- `detectDragGesturesAfterLongPress` đã tự động phân tách: scroll xảy ra ngay lập tức, drag chỉ bắt đầu sau long-press
- Không cần thêm modifier đặc biệt; horizontalScroll + shared scrollState đã đủ

**Status:** ✅ Resolved — Scroll hoạt động dù số part lẻ hay chẵn

---

### Bug 2 — Mắt trái và mắt phải có thể hoán đổi cho nhau ✅

**Vấn đề:** `ZoneId.LEFT_EYE` và `ZoneId.RIGHT_EYE` đều `accepts = PartType.EYE`, nên bất kỳ eye part nào cũng drop được vào cả 2 zone.

**Fix áp dụng (GameViewModel.kt):**
```kotlin
// Trong tryDropPart(), sau khi xác định nearestZone:
val isValidEyePlacement = if (part.type == PartType.EYE && nearestZone != null) {
    when (nearestZone.first.id) {
        ZoneId.LEFT_EYE -> part.side == EyeSide.LEFT
        ZoneId.RIGHT_EYE -> part.side == EyeSide.RIGHT
        else -> true
    }
} else {
    true
}

val canPlace = nearestZone != null &&
    nearestZone.second <= snapThresholdPx &&
    nearestZone.first.accepts == part.type &&
    isValidEyePlacement  // ← Validation thêm vào
```

- LEFT eye (`part.side == EyeSide.LEFT`) chỉ vào LEFT_EYE zone
- RIGHT eye (`part.side == EyeSide.RIGHT`) chỉ vào RIGHT_EYE zone
- Nếu drop sai bên → bounce về tray như sai type
- `checkCompletion()` vẫn dùng `.id` comparison, không thay đổi

**Status:** ✅ Resolved — Eye placement validation hoạt động

---

### Bug 3 — Kích thước 2 mắt render không đều nhau trên FaceCanvas ✅

**Vấn đề:** Khi PNG intrinsic size khác nhau (aspect ratio khác), Image render ra kích thước nhìn không đồng đều. 

**Fix áp dụng:**

1. **FaceCanvas.kt** — Explicit fixed size per type:
```kotlin
.size(
    when (placedPart.type) {
        PartType.EYE -> 64.dp
        PartType.NOSE -> 48.dp
        PartType.MOUTH -> 80.dp
    },
    when (placedPart.type) {
        PartType.EYE -> 56.dp
        PartType.NOSE -> 44.dp
        PartType.MOUTH -> 48.dp
    }
)
```
- Eyes: 64×56.dp (aspect ratio 8:7 = chuẩn)
- Sử dụng ContentScale.Fit để fit PNG vào box này

2. **DraggablePart.kt** — Maintain aspect ratio cho eyes trong tray:
```kotlin
modifier = Modifier.size(
    width = 60.dp,
    height = if (part.type == PartType.EYE) 56.dp else 60.dp
)
```
- Eyes trong tray: 60×56.dp (giữ aspect ratio)
- Non-eyes: 60×60.dp (square)

3. **GameScreen.kt DragOverlay** — Consistent drag preview:
```kotlin
modifier = Modifier.size(
    width = 64.dp,
    height = if (draggedPart.type == PartType.EYE) 56.dp else 64.dp
)
```

**Kết quả:** 
- Cả 2 mắt trong tray hiển thị cùng proportions
- Trên FaceCanvas cả 2 mắt render trong cùng bounding box → nhìn cỡ như nhau
- Drag preview cũng consistent

**Status:** ✅ Resolved — Eye sizing đồng nhất trên tất cả vị trí

---

## 15. Verification Checklist

Sau khi áp dụng 3 fixes, kiểm tra:

- [ ] ✅ Scroll ngang PartsTray hoạt động dù có 9, 10, 11, 12 parts
- [ ] ✅ LEFT eye không thể drop vào RIGHT_EYE zone (bounce về tray)
- [ ] ✅ RIGHT eye không thể drop vào LEFT_EYE zone (bounce về tray)
- [ ] ✅ 2 mắt trên FaceCanvas nhìn cỡ tương tự nhau
- [ ] ✅ 2 mắt trong tray nhìn cỡ tương tự nhau
- [ ] ✅ Drag preview (DragOverlay) hiển thị mắt đúng size
- [ ] ✅ checkCompletion() vẫn hoạt động (dùng .id comparison)
- [ ] ✅ Compile thành công: BUILD SUCCESSFUL

---

## 16. Files Modified (2026-05-04)

| File | Thay đổi |
|------|----------|
| [GameViewModel.kt](app/src/main/java/com/example/emogrow/features/game/ui/GameViewModel.kt) | Thêm eye side validation trong tryDropPart() |
| [FaceCanvas.kt](app/src/main/java/com/example/emogrow/features/game/ui/FaceCanvas.kt) | Explicit fixed sizes per type; removed sizeIn import |
| [DraggablePart.kt](app/src/main/java/com/example/emogrow/features/game/ui/DraggablePart.kt) | Maintain aspect ratio (60×56 for eyes, 60×60 for others) |
| [GameScreen.kt](app/src/main/java/com/example/emogrow/features/game/ui/GameScreen.kt) | DragOverlay sizing consistent (64×56 for eyes) |
| [PartsTray.kt](app/src/main/java/com/example/emogrow/features/game/ui/PartsTray.kt) | No changes (2-row scroll layout verified working) |
3. **No flat white** — card luôn có emotion tint, không bao giờ pure white
4. **Large touch targets** — minimum 88.dp cho mọi draggable element
5. **High contrast** — text `#2D1B0E` trên nền sáng, White trên gradient

---

## 9. Drag & Drop — Implementation rules

```kotlin
// Dùng Modifier.pointerInput với detectDragGesturesAfterLongPress
// (longPress giúp trẻ không vô tình kéo)
Modifier.pointerInput(part.id) {
    detectDragGesturesAfterLongPress(
        onDragStart = { offset -> viewModel.startDrag(part) },
        onDrag = { change, _ -> viewModel.updateDragPosition(change.position) },
        onDragEnd = { viewModel.tryDropPart(...) },
        onDragCancel = { viewModel.cancelDrag() }
    )
}
```

### Drag overlay (trong GameScreen.kt):
- Render khi `uiState.draggedPart != null`
- Dùng `Box(Modifier.fillMaxSize())` overlay với `zIndex = 999f`
- Part card float tại `uiState.dragPosition`, scale 1.15f
- Original card trong tray: alpha = 0.3f khi đang drag

### Drop behavior:
| Tình huống | Hành động |
|-----------|-----------|
| Đúng zone, đúng type | Snap vào zone, spring animation, haptic |
| Sai type | Bounce về tray, wobble animation |
| Không gần zone nào | Return về tray, smooth spring |
| Zone đã có part | Part cũ về tray, part mới vào zone |

---

## 10. Sample Data (trong GameViewModel companion object)

8 rounds: HAPPY → SAD → ANGRY → SCARED → SURPRISED → WORRIED → SHY → PROUD → (lặp lại)

Mỗi round có 6–7 availableParts gồm: 3 loại mắt + mũi + 2-3 loại miệng.

**Quy tắc thêm round mới:** Thêm vào `sampleRounds` list, đặt `isReview = true`
nếu đó là round ôn tập (cảm xúc đã chơi trước đó).

---

## 11. Những thứ KHÔNG được thay đổi khi nhận task mới

- `GameTypes.kt` — data classes và enums
- Interface callback: `onFaceCompleted`, `onCelebrationDone`
- Drop zone positions (mục 6)
- `sampleRounds` data (chỉ được thêm, không sửa round có sẵn)
- `GameDesign` color values (chỉ được thêm, không đổi màu đã có)

---

## 12. Checklist trước khi submit code

- [ ] Không có emoji nào được render trực tiếp ra UI (chỉ dùng trong data, không hiển thị)
- [ ] `checkCompletion()` dùng `.id` string comparison
- [ ] `goToNextRound()` reset đủ 4 fields
- [ ] LEFT_EYE dùng `isMirrored=false`, RIGHT_EYE dùng `isMirrored=true`
- [ ] Mọi kích thước dùng `GameDesign.*` constants, không hardcode
- [ ] Text tiếng Việt, có dấu đầy đủ
- [ ] Comments tiếng Việt cho logic phức tạp
- [ ] Không import thư viện ngoài (chỉ Compose built-ins + coroutines)

## 13. Quy tắc chọn distractor cho availableParts

- Label bộ phận dùng tên chung: "Mắt trái/phải", "Lông mày trái/phải", "Miệng", "Mũi", "Má", "Mồ hôi" — không ghi tên cảm xúc vào label
- Ưu tiên distractor đối lập rõ về hình dạng (vui ↔ buồn ↔ giận)
- Tránh đặt cùng màn: mouth_worried + mouth_scared, mouth_frown + mouth_sad, eyebrows_scared + eyebrows_shy, eye_worried + eye_sad
- Mỗi màn có tối đa 3-4 distractor, không nhồi quá nhiều gây rối trẻ
- Đảm bảo mỗi PartType trong targetFace có ít nhất 1 distractor cùng type để trẻ phải chọn, không đoán mò
```
