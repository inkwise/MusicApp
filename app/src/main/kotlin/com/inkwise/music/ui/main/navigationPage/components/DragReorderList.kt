package com.inkwise.music.ui.main.navigationPage.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

class DragReorderState(
    val listState: LazyListState,
    private val itemCount: Int,
    private val onMove: (Int, Int) -> Unit,
    private val onDragEnd: () -> Unit,
) {
    var draggedItemIndex by mutableStateOf<Int?>(null)
    var dragOffset by mutableStateOf(0f)

    fun dragModifier(index: Int): Modifier {
        return Modifier.pointerInput(index) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    draggedItemIndex = index
                    dragOffset = 0f
                },
                onDragEnd = {
                    draggedItemIndex?.let { from ->
                        val itemHeight = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 72
                        val movedPositions = (dragOffset / itemHeight).roundToInt()
                        val to = (from + movedPositions).coerceIn(0, itemCount - 1)
                        if (from != to) {
                            onMove(from, to)
                        }
                    }
                    draggedItemIndex = null
                    dragOffset = 0f
                    onDragEnd()
                },
                onDragCancel = {
                    draggedItemIndex = null
                    dragOffset = 0f
                },
                onDrag = { change, offset ->
                    change.consume()
                    dragOffset += offset.y
                }
            )
        }
    }

    fun itemOffset(index: Int): IntOffset {
        val from = draggedItemIndex ?: return IntOffset.Zero
        val itemHeight = (listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 72).toFloat()
        if (itemHeight <= 0f) return IntOffset.Zero

        // 被拖拽的 item：直接跟随手指
        if (index == from) {
            return IntOffset(0, dragOffset.roundToInt())
        }

        val target = from.toFloat() + dragOffset / itemHeight

        if (target > from) {
            // 向下拖拽：from 下方的 item 向上位移让位
            if (index <= from || index > target + 1) return IntOffset.Zero
            if (index < target) return IntOffset(0, -itemHeight.roundToInt()) // 完全让位
            // 边界 item：按拖入比例部分让位
            val fraction = target - target.toInt().toFloat()
            return IntOffset(0, (-itemHeight * fraction).roundToInt())
        } else if (target < from) {
            // 向上拖拽：from 上方的 item 向下位移让位
            if (index >= from || index < target) return IntOffset.Zero
            if (index > target) return IntOffset(0, itemHeight.roundToInt()) // 完全让位
            // 边界 item：按拖入比例部分让位
            val fraction = (target.toInt() + 1).toFloat() - target
            return IntOffset(0, (itemHeight * fraction).roundToInt())
        }
        return IntOffset.Zero
    }
}

@Composable
fun rememberDragReorderState(
    listState: LazyListState,
    itemCount: Int,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit,
): DragReorderState {
    return remember(listState, itemCount) {
        DragReorderState(listState, itemCount, onMove, onDragEnd)
    }
}
