package com.grl.sms_wa.utils

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.grl.sms_wa.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

fun RecyclerView.setupSwipeActions(
    context: Context,
    leftIconRes: Int,
    leftLabel: String,
    rightIconRes: Int,
    rightLabel: String,
    leftBackgroundColorRes: Int = R.color.red,
    rightBackgroundColorRes: Int = R.color.col_main,
    onSwipeLeft: (position: Int) -> Unit,
    onSwipeRight: (position: Int) -> Unit
) {
    val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return

            when (direction) {
                ItemTouchHelper.LEFT -> onSwipeLeft(position)
                ItemTouchHelper.RIGHT -> onSwipeRight(position)
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
            RecyclerViewSwipeDecorator.Builder(
                context,
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, leftBackgroundColorRes))
                .addSwipeLeftActionIcon(leftIconRes)
                .addSwipeLeftLabel(leftLabel)
                .addSwipeRightBackgroundColor(ContextCompat.getColor(context, rightBackgroundColorRes))
                .addSwipeRightActionIcon(rightIconRes)
                .addSwipeRightLabel(rightLabel)
                .setSwipeLeftLabelColor(ContextCompat.getColor(context, R.color.white))
                .setSwipeRightLabelColor(ContextCompat.getColor(context, R.color.white))
                .create()
                .decorate()

            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    })
    itemTouchHelper.attachToRecyclerView(this)
}
