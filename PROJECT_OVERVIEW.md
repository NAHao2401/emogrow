# Tong quan du an Emogrow

## 1) Muc tieu va doi tuong
- Muc tieu: ung dung hoc cam xuc cho tre em, uu tien nhom 4-8 tuoi va tre kho nhan biet cam xuc.
- Dinh huong UI: toan bo giao dien bang tieng Viet, do uu tien cao cho tre chua biet doc.

## 2) Cong nghe su dung
- Nen tang: Android.
- Ngon ngu: Kotlin.
- UI: Jetpack Compose (khong dung XML layout).
- Kien truc: UI theo tinh huong (screen-based), state duoc quan ly trong ViewModel.

## 3) Cac thanh phan chinh
- UI layer: cac man hinh va thanh phan hien thi (Compose).
- Domain/data layer: quan ly level cam xuc va tien trinh (AlbumManager, EmotionLevel).
- Tai nguyen do hoa: PNG cho cac bo phan khuon mat (mat, mui, mieng).

## 4) Cac man hinh trong app
1. Hoc cam xuc
   - Flashcard hoc cam xuc.
2. Tro choi ghep mat cam xuc (chi tiet o muc 6).
3. Nhat ky cam xuc
   - Gieo hat cam xuc hang ngay.
4. Xem lai hanh trinh
   - Tong hop/nhin lai qua trinh cam xuc.

## 5) Luong hoat dong tong quan
- Nguoi dung vao app va chon man hinh tu menu chinh.
- Hoat dong hoc: xem flashcard cam xuc.
- Hoat dong tro choi: ghep mat cam xuc theo yeu cau.
- Hoat dong nhat ky: ghi nhan cam xuc hang ngay.
- Hoat dong xem lai: theo doi tien trinh va thanh tuu.

---

## 6) Chi tiet tro choi ghep mat cam xuc

### 6.1 Muc tieu gameplay
- Nguoi choi keo tha cac bo phan (mat, mui, mieng) vao dung vi tri tren khuon mat.
- Muc tieu la tao dung khuon mat khop voi cam xuc duoc goi y.

### 6.2 Cau truc file (game UI)
- GameScreen.kt: man hinh chinh, xu ly drag overlay va vong doi round.
- FaceCanvas.kt: ve khuon mat va 4 drop zones.
- PartsTray.kt: khay bo phan o day man hinh, cuon ngang 2 hang.
- DraggablePart.kt: the bo phan co the keo tha.
- FacePartDrawing.kt: render PNG cho bo phan khuon mat.
- GameViewModel.kt: quan ly state, drag/drop va kiem tra dung sai.
- GameTypes.kt: kieu du lieu (EmotionType, FacePart, DropZone, GameRound, GameUiState).
- GameDesign.kt: design system (mau, size, spacing, typography).

### 6.3 Luong choi (state flow)
1) Nguoi choi long-press tren mot bo phan trong khay.
2) startDrag() cap nhat draggedPart va bat dau drag overlay.
3) updateDragPosition() cap nhat toa do keo tha.
4) tryDropPart() kiem tra vi tri drop va zone gan nhat:
   - Neu dung zone va dung loai bo phan -> dat vao placedParts.
   - Neu sai -> bo phan quay ve tray.
5) checkCompletion() kiem tra toan bo zone da dung hay chua.
6) Neu hoan thanh:
   - isCompleted = true.
   - onFaceCompleted() duoc goi (confetti + nhan dan bay len album).
7) Sau khi animation xong: goToNextRound() reset state va sang vong moi.

### 6.4 Drop zones va quy tac snap
- Co 4 zone: LEFT_EYE, RIGHT_EYE, NOSE, MOUTH.
- Snap tolerance: 80.dp tinh tu center cua zone.
- Quy tac mat trai/phai:
  - LEFT_EYE chi nhan mat trai.
  - RIGHT_EYE chi nhan mat phai.

### 6.5 Hanh vi tuong tac
- Tap vao bo phan da dat de go bo phan khoi mat.
- Zone rong hien dashed border.
- Hover dung loai bo phan: zone glow + pulse.
- Hover sai loai bo phan: vien do canh bao.

### 6.6 Thiet ke giao dien
- Nen man hinh am, vui, than thien.
- The bo phan va card prompt dung gradient theo cam xuc.
- Quy tac UI bat buoc:
  - Vien dam (bold outline 2.dp).
  - Hieu ung do sau 3D cho element tuong tac.
  - Khong dung nen trang phang; uu tien emotion tint.
  - Kich thuoc cham lon (>= 88.dp).
  - Tuong phan cao (text dam tren nen sang).

### 6.7 Tai nguyen do hoa
- PNG rieng cho mat trai va mat phai.
- Tray dung file mat trai; face canvas dung dung trai/phai theo zone.
- Kich thuoc render on-canvas va trong tray duoc co dinh theo loai bo phan.

---

## 7) Quan ly level cam xuc (AlbumManager)
- Luu danh sach level, trang thai hoan thanh, mo khoa, thong tin mo ta.
- Tu dong mo khoa level dau tien.
- Hoan thanh level -> mo khoa level tiep theo (neu co).
- Ho tro replay, reset tat ca level.
- Du lieu duoc cache trong DataStore va co the dong bo API.

## 8) Mo rong trong tuong lai
- Bo sung nhieu bo phan/kieu khuon mat hon.
- Them round theo chu de hoac cap do kho.
- Ghi nhan thong ke chi tiet cho tung cam xuc.
