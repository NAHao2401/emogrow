package com.example.emogrow.features.review.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emogrow.R
import com.example.emogrow.features.review.model.Book
import com.example.emogrow.features.review.model.ShelfData
import com.example.emogrow.features.review.model.Sticker
import com.example.emogrow.features.review.ui.components.BookDetailDialog
import com.example.emogrow.features.review.ui.components.StickerAlbumModal
import com.example.emogrow.features.review.ui.components.StickerPopupDialog
import com.example.emogrow.features.review.viewmodel.ReviewSharedViewModel
import com.example.emogrow.ui.theme.*
import com.example.emogrow.ui.theme.BookRed

@Composable
fun KnowledgeShelfScreen(
    onBack: () -> Unit,
    viewModel: ReviewSharedViewModel,
    onNavigateToEmotionJarWithHighlight: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val shelves = viewModel.shelves
    val allStickers = viewModel.allStickersList

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyBlue, Cream, LightGreen)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            // A. Header Card
            ShelfHeader(
                onBack = onBack,
                onAlbumClick = { viewModel.openStickerModal() }
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // B. 3 Book Shelves
                shelves.forEach { shelf ->
                    BookShelfItem(
                        shelfData = shelf,
                        readBooks = uiState.readBooks,
                        onBookClick = { viewModel.openBookDialog(it) }
                    )
                }

                // C. Sticker Album Section
                StickerAlbumSection(
                    stickers = allStickers.take(12),
                    unlockedStickers = uiState.unlockedStickers,
                    onStickerClick = { viewModel.openStickerPopup(it) }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Teddy Mascot at bottom right
        TeddyMascot(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            isBubbleOpen = uiState.isTeddyBubbleOpen,
            suggestedBooks = uiState.suggestedBooks,
            onTeddyClick = { viewModel.toggleTeddyBubble() },
            onDismissBubble = { viewModel.closeTeddyBubble() },
            onBookClick = { book -> viewModel.openBookDialog(book) }
        )

        // Bottom Navigation Bar
        SharedBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTab = "shelf",
            onEmotionClick = onBack,
            onShelfClick = {}
        )

        // Book Detail Dialog
        val selectedBook = uiState.selectedBook
        BookDetailDialog(
            book = selectedBook,
            isRead = selectedBook?.let { viewModel.isBookRead(it.id) } ?: false,
            isOpen = uiState.isBookDialogOpen,
            onDismiss = { viewModel.closeBookDialog() },
            onMarkAsRead = {
                selectedBook?.let { viewModel.markBookAsRead(it.id) }
            },
            onAddEmotionNote = {
                viewModel.closeBookDialog()
                // Navigate back to emotion jar with the emotion category highlighted
                selectedBook?.let { book ->
                    onNavigateToEmotionJarWithHighlight(book.category)
                }
            }
        )

        // Sticker Album Modal
        StickerAlbumModal(
            stickers = allStickers,
            unlockedStickers = uiState.unlockedStickers,
            isOpen = uiState.isStickerModalOpen,
            onDismiss = { viewModel.closeStickerModal() },
            onStickerClick = { sticker ->
                val book = viewModel.getStickerUnlockBook(sticker)
                viewModel.openStickerPopup(sticker)
            }
        )

        // Sticker Popup
        val selectedSticker = uiState.selectedStickerForPopup
        StickerPopupDialog(
            sticker = selectedSticker,
            isUnlocked = selectedSticker?.let { viewModel.isStickerUnlocked(it.id) } ?: false,
            relatedBookTitle = selectedSticker?.let { viewModel.getStickerUnlockBook(it)?.title },
            isOpen = uiState.isStickerPopupOpen,
            onDismiss = { viewModel.closeStickerPopup() },
            onNavigateToBook = {
                selectedSticker?.let { sticker ->
                    viewModel.getStickerUnlockBook(sticker)?.let { book ->
                        viewModel.closeStickerPopup()
                        viewModel.closeStickerModal()
                        viewModel.openBookDialog(book)
                    }
                }
            }
        )
    }
}

@Composable
private fun ShelfHeader(onBack: () -> Unit, onAlbumClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.DarkGray
            )
        }

        Text(
            text = "Kệ Sách Tri Thức",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.5f))
        )

        Surface(
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(18.dp),
            color = Wheat.copy(alpha = 0.9f),
            shadowElevation = 2.dp,
            onClick = onAlbumClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CollectionsBookmark,
                    contentDescription = null,
                    tint = Color(0xFF8B4513),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Album Sticker",
                    fontSize = 12.sp,
                    color = Color(0xFF8B4513),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BookShelfItem(
    shelfData: ShelfData,
    readBooks: Set<String>,
    onBookClick: (Book) -> Unit
) {
    Column {
        Text(
            text = shelfData.date,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(BrownWood.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                repeat(5) { i ->
                    drawLine(
                        color = BrownWood.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, (size.height / 5) * i),
                        end = androidx.compose.ui.geometry.Offset(size.width, (size.height / 5) * i),
                        strokeWidth = 1f
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                shelfData.books.forEach { book ->
                    BookItem(
                        book = book,
                        isRead = readBooks.contains(book.id),
                        onClick = { onBookClick(book) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BrownWood.copy(alpha = 0.9f),
                                BrownWood.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(text = "🪴", fontSize = 24.sp)
            }
        }
    }
}

@Composable
private fun BookItem(
    book: Book,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val bookImageRes = when (book.id) {
        "1", "2", "10" -> R.drawable.img_book_red
        "3" -> R.drawable.img_book_orange
        "4", "7", "9" -> R.drawable.img_book_yellow
        "5" -> R.drawable.img_book_blue
        "6", "8" -> R.drawable.img_book_pink
        else -> R.drawable.img_book_purple
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(75.dp)
                .height(95.dp)
                .shadow(4.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = bookImageRes),
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Read badge
            if (isRead) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = book.title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = book.author,
            fontSize = 8.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StickerAlbumSection(
    stickers: List<Sticker>,
    unlockedStickers: Set<String>,
    onStickerClick: (Sticker) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFDF5E6).copy(alpha = 0.9f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(StickerGold.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📔", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Album Sticker - Đã có kèm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B4513)
                )
            }
            Text(text = "🌸", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(stickers) { sticker ->
                StickerItem(
                    sticker = sticker,
                    isUnlocked = unlockedStickers.contains(sticker.id),
                    onClick = { onStickerClick(sticker) }
                )
            }
        }
    }
}

@Composable
private fun StickerItem(
    sticker: Sticker,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val stickerImageRes = when (sticker.id) {
        "1", "2" -> R.drawable.stk_cloud_rainbow
        "3", "7" -> R.drawable.stk_dinosaur
        "4", "8" -> R.drawable.stk_air_balloon
        else -> R.drawable.stk_cloud_rainbow
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(1.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = StickerGold,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = stickerImageRes),
                contentDescription = sticker.name,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(if (isUnlocked) 1f else 0.5f)
            )
            Text(
                text = sticker.name,
                fontSize = 8.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun TeddyMascot(
    modifier: Modifier = Modifier,
    isBubbleOpen: Boolean,
    suggestedBooks: List<Book>,
    onTeddyClick: () -> Unit,
    onDismissBubble: () -> Unit,
    onBookClick: (Book) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isBubbleOpen) 5f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "teddyRotation"
    )

    Box(
        modifier = modifier
            .size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Bubble (if open)
        if (isBubbleOpen && suggestedBooks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .offset(x = (-80).dp, y = (-80).dp)
                    .width(150.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Bạn có muốn đọc một câu chuyện vui vẻ hôm nay không?",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                suggestedBooks.take(3).forEach { book ->
                    Text(
                        text = "📚 ${book.title}",
                        fontSize = 10.sp,
                        color = DarkPurple,
                        modifier = Modifier
                            .clickable { onBookClick(book) }
                            .padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✕",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onDismissBubble() }
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.img_mascot_bear),
            contentDescription = "Teddy Mascot",
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
                .clickable(onClick = onTeddyClick)
        )
    }
}


private val DarkPurple = Color(0xFF6A1B9A)