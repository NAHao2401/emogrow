package com.example.emogrow.features.review.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.runtime.LaunchedEffect

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun KnowledgeShelfScreen(
    onBack: () -> Unit,
    viewModel: ReviewSharedViewModel,
    onNavigateToEmotionJarWithHighlight: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val shelves by viewModel.shelves.collectAsState()
    val allStickers = viewModel.allStickersList
    val highlightedDate = uiState.highlightedDate
    val listState = rememberLazyListState()

    // Glow + highlight the book for selected bead date — no auto-open
    LaunchedEffect(uiState.scrollToDate) {
        uiState.scrollToDate?.let { date ->
            viewModel.prepareForDate(date)
            viewModel.clearScrollToDate()
        }
    }

    // Auto-scroll to glowing shelf
    LaunchedEffect(highlightedDate, shelves) {
        if (highlightedDate != null && shelves.isNotEmpty()) {
            val index = shelves.indexOfFirst { shelfMatchesDate(it.date, highlightedDate) }
            if (index >= 0) {
                listState.animateScrollToItem(index.coerceAtLeast(0), scrollOffset = -60)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9C4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // A. Header Card
            ShelfHeader(
                modifier = Modifier.zIndex(1f),
                onBack = onBack,
                onAlbumClick = { viewModel.openStickerModal() }
            )

            // Scrollable content - proper padding to avoid overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF8B4513).copy(alpha = 0.8f))
                    .padding(8.dp)
                    .background(Color(0xFFD2B48C), RoundedCornerShape(16.dp))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    userScrollEnabled = true
                ) {
                    items(shelves, key = { it.id }) { shelf ->
                        BookShelfItem(
                            shelfData = shelf,
                            readBooks = uiState.readBooks,
                            highlightedDate = highlightedDate,
                            onBookClick = { viewModel.openBookDialog(it) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
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
private fun ShelfHeader(modifier: Modifier = Modifier, onBack: () -> Unit, onAlbumClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nav_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = "Kệ Sách Tri Thức",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5D4037),
            modifier = Modifier.shadow(0.dp)
        )

        Surface(
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5DEB3),
            shadowElevation = 2.dp,
            onClick = onAlbumClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF3498DB), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CollectionsBookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "Album",
                        fontSize = 10.sp,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                    Text(
                        text = "Sticker",
                        fontSize = 10.sp,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BookShelfItem(
    shelfData: ShelfData,
    readBooks: Set<String>,
    highlightedDate: String?,
    onBookClick: (Book) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF3498DB), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📅", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = shelfData.date,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Books shelf surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF8B4513).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val plankWidth = size.width / 10
                    for (i in 1..9) {
                        drawLine(
                            color = Color(0xFF5D4037).copy(alpha = 0.2f),
                            start = androidx.compose.ui.geometry.Offset(i * plankWidth, 0f),
                            end = androidx.compose.ui.geometry.Offset(i * plankWidth, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                items(shelfData.books, key = { it.id }) { book ->
                    BookItem(
                        book = book,
                        isRead = readBooks.contains(book.id),
                        isHighlighted = highlightedDate != null && shelfMatchesDate(shelfData.date, highlightedDate),
                        modifier = Modifier.width(100.dp),   // cố định chiều rộng mỗi cuốn
                        onClick = { onBookClick(book) }
                    )
                }
            }

            // Shelf bottom ledge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF5D4037),
                                Color(0xFF3E2723)
                            )
                        ),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private val shelfDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private const val TAG = "ShelfMatch"

@RequiresApi(Build.VERSION_CODES.O)
private fun shelfMatchesDate(shelfDate: String, highlightedDate: String): Boolean {
    val datePart = shelfDate.substringAfter(" - ", shelfDate.takeLast(10))
    Log.d(TAG, "shelfDate=$shelfDate | datePart=$datePart | highlightedDate=$highlightedDate")
    return try {
        val shelfLocalDate = LocalDate.parse(datePart, shelfDateFormatter)
        val highlightLocalDate = LocalDate.parse(highlightedDate)
        Log.d(TAG, "parsed shelf=$shelfLocalDate | highlight=$highlightLocalDate | match=${shelfLocalDate == highlightLocalDate}")
        shelfLocalDate == highlightLocalDate
    } catch (e: Exception) {
        Log.e(TAG, "parse error: ${e.message}")
        false
    }
}

@Composable
private fun BookItem(
    book: Book,
    isRead: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val glowTransition = rememberInfiniteTransition(label = "bookGlow")
    val animatedGlowAlpha by glowTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowAlpha = if (isHighlighted) animatedGlowAlpha else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
                .then(
                    if (isHighlighted) {
                        Modifier
                            .shadow(
                                elevation = (24 * glowAlpha).dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = GoldStar.copy(alpha = glowAlpha),
                                ambientColor = GoldStar.copy(alpha = glowAlpha)
                            )
                            .background(GoldStar.copy(alpha = 0.25f * glowAlpha), RoundedCornerShape(16.dp))
                            .border(5.dp, GoldStar.copy(alpha = 0.9f * glowAlpha), RoundedCornerShape(16.dp))
                            .padding(6.dp)
                    } else {
                        Modifier.shadow(4.dp, RoundedCornerShape(8.dp))
                    }
                )
                .clip(RoundedCornerShape(8.dp))
                .background(book.color),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = book.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp
                )

                Text(
                    text = book.emoji,
                    fontSize = 32.sp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ngày gieo mầm",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "20/05/2024",
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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
    }
}

@Composable
private fun StickerAlbumSection(
    stickers: List<Sticker>,
    unlockedStickers: Set<String>,
    modifier: Modifier = Modifier,
    onStickerClick: (Sticker) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFF1C40F), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📔", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cuốn Album - Bộ sưu tập Sticker",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(Color(0xFFFDF5E6), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF8B4513).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            // Binder rings
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        shape = CircleShape,
                        color = Color.LightGray,
                        shadowElevation = 1.dp
                    ) {}
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 64.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = true
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
            .size(64.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = stickerImageRes),
            contentDescription = sticker.name,
            modifier = Modifier
                .size(48.dp)
                .alpha(if (isUnlocked) 1f else 0.3f)
        )
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
    Box(
        modifier = modifier
            .size(120.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Bubble (if open)
        if (isBubbleOpen && suggestedBooks.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .offset(y = (-100).dp)
                    .width(180.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFF1C40F), RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "Bạn có muốn đọc một câu chuyện vui vẻ hôm nay không?",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                suggestedBooks.take(2).forEach { book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookClick(book) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = book.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = book.title,
                            fontSize = 11.sp,
                            color = Color(0xFF6A1B9A),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "Đóng",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onDismissBubble() }
                        .padding(top = 4.dp)
                )
            }
        }

        // The Puppy/Mascot reading a book
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(onClick = onTeddyClick),
            contentAlignment = Alignment.BottomEnd
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_bear),
                contentDescription = "Teddy Mascot",
                modifier = Modifier.size(80.dp)
            )
            
            // Mock book the mascot is reading
            Surface(
                modifier = Modifier
                    .size(40.dp, 30.dp)
                    .offset(x = (-10).dp, y = (-5).dp),
                color = Color(0xFFF1C40F),
                shape = RoundedCornerShape(4.dp),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "📖", fontSize = 20.sp)
                }
            }
        }
    }
}


