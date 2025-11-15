package com.cheermateapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Custom spinner adapter that displays an icon (emoji) and text
 */
class IconSpinnerAdapter(
    context: Context,
    private val items: List<SpinnerItem>
) : ArrayAdapter<IconSpinnerAdapter.SpinnerItem>(context, R.layout.item_spinner_with_icon, items) {

    data class SpinnerItem(
        val icon: String,  // Emoji or unicode character
        val text: String
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_with_icon, parent, false)

        val item = items[position]
        
        val tvIcon = view.findViewById<TextView>(R.id.tvIcon)
        val tvText = view.findViewById<TextView>(R.id.tvText)

        tvIcon.text = item.icon
        tvText.text = item.text

        return view
    }
}
