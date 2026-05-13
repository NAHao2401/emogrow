# CONTEXT for the Album (Game UI)

This repository is an Android app written in Kotlin with Jetpack Compose UI.

The album/game screen UI lives in `app/src/main/java/com/example/emogrow/features/game/ui/` and is centered around `GameScreen.kt`. It presents a face-building activity where the player drags parts onto a face to match an emotion prompt.

UI structure and behavior:
- Overall layout: a full-screen column on a warm background with system bar padding. It has a top prompt card, a central face canvas, and a bottom parts tray.
- Prompt header: a rounded card with an emotion-themed gradient, large prompt text, and a separate emoji line.
- Face canvas: a soft organic face shape with gold border; four drop zones (eyes, nose, mouth) positioned relative to the face.
- Drop zones: dashed outlines appear when empty; zones pulse and glow when a compatible part is dragged nearby; tapping a placed part removes it.
- Parts tray: a raised panel with a grab handle and the label "Chọn bộ phận 👇"; parts are shown in a horizontal list with two rows per column.
- Draggable parts: each part is a rounded card with a tint based on emotion; long-press drag is used; placed/dragging parts fade to 30% opacity; cards do a small wiggle on appear.
- Drag overlay: while dragging, the part is rendered larger and follows the pointer above all UI.
- Completion feedback: confetti animation fills the screen; then a sticker flies to the top-right corner and triggers level completion.

Supporting UI files:
- `GameDesign.kt` defines colors, spacing, sizes, and emotion gradients.
- `FaceCanvas.kt` defines the face shape, drop zones, and hover/placed states.
- `PartsTray.kt` and `DraggablePart.kt` define the parts list and drag interaction cards.
- `FacePartDrawing.kt` maps part IDs to drawable assets (eyes, nose, mouths).

This context describes the current album/game UI and how the player interacts with it.