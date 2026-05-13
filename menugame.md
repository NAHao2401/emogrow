# Mo ta giao dien AlbumManager

Tai lieu nay tom tat giao dien cong khai va hanh vi chinh cua lop AlbumManager va data class EmotionLevel trong [app/src/main/java/com/example/emogrow/data/repository/AlbumManager.kt](app/src/main/java/com/example/emogrow/data/repository/AlbumManager.kt).

## EmotionLevel (data class)

Muc do cam xuc duoc luu tru va hien thi trong album.

- Thuoc tinh:
  - id: Int
  - emotionName: String
  - emoji: String
  - description: String
  - imageUrl: String
  - isUnlocked: Boolean
  - isCompleted: Boolean
  - completedAt: Long? (thoi diem hoan thanh, milliseconds)
  - replayCount: Int

## AlbumManager (singleton)

Quan ly danh sach level, cache DataStore, va dong bo du lieu tu API.

### Khoi tao va trang thai

- Cung cap instance qua AlbumManager.getInstance(context).
- Tu dong nap cache khi khoi tao, neu khong co thi seed du lieu mac dinh.
- Tu dong dam bao level dau tien duoc mo khoa.
- Tu dong goi API de refresh du lieu (neu co).

### Luong du lieu (StateFlow)

- levels: StateFlow<List<EmotionLevel>>
  - Danh sach level hien tai, duoc cap nhat khi cache/API thay doi.
- getCompletionProgress(): StateFlow<Pair<Int, Int>>
  - Tra ve cap (so level da hoan thanh, tong so level).

### Ham truy van

- getAllLevels(): StateFlow<List<EmotionLevel>>
- getLevelById(id: Int): EmotionLevel?
- canPlayLevel(levelId: Int): Boolean
  - true neu level da mo khoa hoac da hoan thanh.
- getLevelProgress(levelId: Int): Int
  - 100 neu da hoan thanh, nguoc lai 0.

### Ham cap nhat (suspend)

Tat ca ham ben duoi bat dong bo va duoc bao ve boi Mutex de tranh race.

- unlockLevel(levelId: Int): Boolean
  - Mo khoa level neu level truoc do da hoan thanh.
  - Tra ve false neu level khong ton tai/da mo khoa/da hoan thanh.
- completeLevel(levelId: Int): Boolean
  - Danh dau hoan thanh level, luu completedAt.
  - Tu dong mo khoa level tiep theo (neu co).
- replayLevel(levelId: Int): Boolean
  - Tang replayCount neu level da hoan thanh.
- resetAllLevels()
  - Dat lai tat ca level ve chua hoan thanh, chi level dau tien duoc mo khoa.

### Luu tru va dong bo

- DataStore Preferences dung key "levels_json".
- Danh sach level duoc serialize bang Gson.
- Khi co du lieu tu API, se merge voi cache theo id:
  - Giu lai trang thai local (isUnlocked, isCompleted, completedAt, replayCount).
  - Du lieu noi dung (ten, mo ta, imageUrl) lay tu API.

### Ghi chu hanh vi

- levels luon duoc sap xep theo id khi khoi tao, unlock, complete, va refresh.
- completeLevel se mo khoa level tiep theo, neu ton tai.
- unlockLevel khong cho mo khoa neu level truoc chua hoan thanh.
