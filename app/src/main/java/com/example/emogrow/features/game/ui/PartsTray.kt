package com.example.emogrow.features.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row

@Composable
fun PartsTray(
    availableParts: List<FacePart>,
    placedPartIds: Set<String>,
    isDragging: Boolean,
    onDragStart: (FacePart, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

    Box(
        modifier = modifier
            .shadow(elevation = 20.dp, shape = shape)
            .background(Color(0xFFFFFCF7), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0xFFDDD0C0), RoundedCornerShape(2.dp))
            )
            Text(
                text = "Chọn bộ phận 👇",
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GameDesign.textMid
            )

            val columns = availableParts.chunked(2)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = !isDragging
            ) {
                items(columns.size) { index ->
                    val topPart = columns[index].first()
                    val bottomPart = columns[index].getOrNull(1)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val topPlaced = placedPartIds.contains(topPart.uniqueKey)
                        DraggablePart(
                            part = topPart,
                            isPlaced = topPlaced,
                            onDragStart = onDragStart,
                            onDragMove = onDragMove,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel
                        )

                        if (bottomPart != null) {
                            val bottomPlaced = placedPartIds.contains(bottomPart.uniqueKey)
                            DraggablePart(
                                part = bottomPart,
                                isPlaced = bottomPlaced,
                                onDragStart = onDragStart,
                                onDragMove = onDragMove,
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragCancel
                            )
                        } else {
                            Spacer(modifier = Modifier.height(96.dp))
                        }
                    }
                }
            }
        }
    }
}


