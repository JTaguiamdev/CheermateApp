package com.cheermateapp.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cheermateapp.R

/**
 * Helper class for implementing swipe gestures on RecyclerView
 * - Swipe right: Mark as completed (green background with check icon)
 * - Swipe left: Delete task (red background with delete icon)
 */
class SwipeGestureHelper(
    private val context: Context,
    private val onSwipeComplete: (Int) -> Unit,
    private val onSwipeDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    // Colors and drawables
    private val completeColor = ContextCompat.getColor(context, R.color.success_green)
    private val deleteColor = ContextCompat.getColor(context, R.color.error_red)
    private val completeBackground = ColorDrawable(completeColor)
    private val deleteBackground = ColorDrawable(deleteColor)
    
    // Icons
    private val completeIcon = ContextCompat.getDrawable(context, R.drawable.ic_check)?.apply {
        setTint(Color.WHITE)
    }
    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)?.apply {
        setTint(Color.WHITE)
    }
    
    // Paint for text
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // We don't support move
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        
        when (direction) {
            ItemTouchHelper.RIGHT -> {
                // Swipe right: Mark as completed
                onSwipeComplete(position)
            }
            ItemTouchHelper.LEFT -> {
                // Swipe left: Delete task
                onSwipeDelete(position)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - (completeIcon?.intrinsicHeight ?: 0)) / 2
        val iconTop = itemView.top + (itemView.height - (completeIcon?.intrinsicHeight ?: 0)) / 2
        val iconBottom = iconTop + (completeIcon?.intrinsicHeight ?: 0)

        when {
            dX > 0 -> { // Swiping to the right (Complete)
                completeBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                completeBackground.draw(c)

                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + (completeIcon?.intrinsicWidth ?: 0)
                
                completeIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                completeIcon?.draw(c)
                
                // Draw text
                val textX = itemView.left + (dX / 2)
                val textY = itemView.top + (itemView.height / 2) + 60f
                c.drawText("Complete", textX, textY, textPaint)
            }
            
            dX < 0 -> { // Swiping to the left (Delete)
                deleteBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteBackground.draw(c)

                val iconLeft = itemView.right - iconMargin - (deleteIcon?.intrinsicWidth ?: 0)
                val iconRight = itemView.right - iconMargin
                
                deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon?.draw(c)
                
                // Draw text
                val textX = itemView.right + (dX / 2)
                val textY = itemView.top + (itemView.height / 2) + 60f
                c.drawText("Delete", textX, textY, textPaint)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.3f // Require 30% swipe to trigger action
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 1.5f // Make it easier to trigger with fast swipes
    }
}