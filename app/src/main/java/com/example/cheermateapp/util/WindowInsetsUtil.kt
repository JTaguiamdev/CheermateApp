package com.example.cheermateapp.util

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Utility object for handling WindowInsets to apply dynamic padding for system bars
 */
object WindowInsetsUtil {

    /**
     * Apply system bar insets as padding to a view
     * @param view The view to apply padding to
     * @param left Whether to apply left inset
     * @param top Whether to apply top inset
     * @param right Whether to apply right inset
     * @param bottom Whether to apply bottom inset
     */
    fun applySystemBarInsets(
        view: View,
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val initialPadding = recordInitialPaddingForView(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            v.updatePadding(
                left = initialPadding.left + if (left) insets.left else 0,
                top = initialPadding.top + if (top) insets.top else 0,
                right = initialPadding.right + if (right) insets.right else 0,
                bottom = initialPadding.bottom + if (bottom) insets.bottom else 0
            )
            
            windowInsets
        }
    }

    /**
     * Apply status bar insets as top padding to a view
     * @param view The view to apply padding to
     */
    fun applyStatusBarInsets(view: View) {
        val initialPadding = recordInitialPaddingForView(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            
            v.updatePadding(
                top = initialPadding.top + insets.top
            )
            
            windowInsets
        }
    }

    /**
     * Apply navigation bar insets as bottom padding to a view
     * @param view The view to apply padding to
     */
    fun applyNavigationBarInsets(view: View) {
        val initialPadding = recordInitialPaddingForView(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            v.updatePadding(
                bottom = initialPadding.bottom + insets.bottom
            )
            
            windowInsets
        }
    }

    /**
     * Apply both status bar and navigation bar insets
     * @param view The view to apply padding to
     */
    fun applySystemBarsInsets(view: View) {
        val initialPadding = recordInitialPaddingForView(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            v.updatePadding(
                top = initialPadding.top + insets.top,
                bottom = initialPadding.bottom + insets.bottom
            )
            
            windowInsets
        }
    }

    /**
     * Apply insets as margin instead of padding
     * @param view The view to apply margin to
     * @param left Whether to apply left inset
     * @param top Whether to apply top inset
     * @param right Whether to apply right inset
     * @param bottom Whether to apply bottom inset
     */
    fun applySystemBarInsetsAsMargin(
        view: View,
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val initialMargin = recordInitialMarginForView(view)
        
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.let {
                it.leftMargin = initialMargin.left + if (left) insets.left else 0
                it.topMargin = initialMargin.top + if (top) insets.top else 0
                it.rightMargin = initialMargin.right + if (right) insets.right else 0
                it.bottomMargin = initialMargin.bottom + if (bottom) insets.bottom else 0
                v.layoutParams = it
            }
            
            windowInsets
        }
    }

    /**
     * Record initial padding for a view to preserve it when applying insets
     */
    private fun recordInitialPaddingForView(view: View): InitialPadding {
        return InitialPadding(
            view.paddingLeft,
            view.paddingTop,
            view.paddingRight,
            view.paddingBottom
        )
    }

    /**
     * Record initial margin for a view to preserve it when applying insets
     */
    private fun recordInitialMarginForView(view: View): InitialMargin {
        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
        return InitialMargin(
            layoutParams?.leftMargin ?: 0,
            layoutParams?.topMargin ?: 0,
            layoutParams?.rightMargin ?: 0,
            layoutParams?.bottomMargin ?: 0
        )
    }

    private data class InitialPadding(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    private data class InitialMargin(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
