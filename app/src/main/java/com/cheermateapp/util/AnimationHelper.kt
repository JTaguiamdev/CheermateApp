package com.cheermateapp.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper object for common UI animations in CheermateApp.
 * Provides reusable animation methods for various UI components.
 */
object AnimationHelper {

    // Animation duration constants
    private const val DURATION_SHORT = 150L
    private const val DURATION_MEDIUM = 300L
    private const val DURATION_LONG = 500L

    /**
     * Fade in a view with optional duration
     */
    fun fadeIn(view: View, duration: Long = DURATION_MEDIUM) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Fade out a view with optional duration
     */
    fun fadeOut(view: View, duration: Long = DURATION_MEDIUM, hideAfter: Boolean = true) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (hideAfter) {
                    view.visibility = View.GONE
                }
            }
            .start()
    }

    /**
     * Crossfade between two views
     */
    fun crossfade(fadeOutView: View, fadeInView: View, duration: Long = DURATION_MEDIUM) {
        fadeInView.alpha = 0f
        fadeInView.visibility = View.VISIBLE

        fadeOutView.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                fadeOutView.visibility = View.GONE
            }
            .start()

        fadeInView.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Pulse animation for emphasis (e.g., task completion)
     */
    fun pulse(view: View, scale: Float = 1.2f) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scale, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scale, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            duration = DURATION_MEDIUM
            interpolator = OvershootInterpolator()
            start()
        }
    }

    /**
     * Bounce animation for playful feedback
     */
    fun bounce(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 0.9f, 1.05f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 0.9f, 1.05f, 0.95f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 400
            start()
        }
    }

    /**
     * Shake animation for error/attention
     */
    fun shake(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 8f, -8f, 5f, -5f, 0f).apply {
            duration = DURATION_LONG
            start()
        }
    }

    /**
     * Slide in from right animation
     */
    fun slideInFromRight(view: View, duration: Long = DURATION_MEDIUM) {
        view.visibility = View.VISIBLE
        view.translationX = view.width.toFloat()
        view.alpha = 0f
        
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Slide out to left animation
     */
    fun slideOutToLeft(view: View, duration: Long = DURATION_MEDIUM, hideAfter: Boolean = true) {
        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (hideAfter) {
                    view.visibility = View.GONE
                }
                view.translationX = 0f
            }
            .start()
    }

    /**
     * Scale up animation (entrance)
     */
    fun scaleUp(view: View, duration: Long = DURATION_MEDIUM) {
        view.visibility = View.VISIBLE
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 0f
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    /**
     * Scale down animation (exit)
     */
    fun scaleDown(view: View, duration: Long = DURATION_MEDIUM, hideAfter: Boolean = true) {
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                if (hideAfter) {
                    view.visibility = View.GONE
                }
                view.scaleX = 1f
                view.scaleY = 1f
            }
            .start()
    }

    /**
     * Animate progress bar weight smoothly
     */
    fun animateProgressWeight(view: View, targetWeight: Float, duration: Long = DURATION_LONG) {
        val params = view.layoutParams as? LinearLayout.LayoutParams ?: return
        val startWeight = params.weight
        
        ValueAnimator.ofFloat(startWeight, targetWeight).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                params.weight = animator.animatedValue as Float
                view.layoutParams = params
            }
            start()
        }
    }

    /**
     * Configure RecyclerView with default item animations
     */
    fun setupRecyclerViewItemAnimator(recyclerView: RecyclerView) {
        recyclerView.itemAnimator = DefaultItemAnimator().apply {
            addDuration = DURATION_MEDIUM
            removeDuration = DURATION_MEDIUM
            changeDuration = DURATION_MEDIUM
            moveDuration = DURATION_MEDIUM
        }
    }

    /**
     * Apply layout animation to RecyclerView
     */
    fun applyLayoutAnimation(recyclerView: RecyclerView, animationResId: Int) {
        try {
            val layoutAnimation = AnimationUtils.loadLayoutAnimation(
                recyclerView.context,
                animationResId
            )
            recyclerView.layoutAnimation = layoutAnimation
        } catch (e: Exception) {
            android.util.Log.e("AnimationHelper", "Failed to load layout animation (resId: $animationResId)", e)
        }
    }

    /**
     * Run layout animation on RecyclerView (useful after data changes)
     */
    fun runLayoutAnimation(recyclerView: RecyclerView) {
        recyclerView.scheduleLayoutAnimation()
    }

    /**
     * Task completion celebration animation
     */
    fun celebrateTaskCompletion(view: View) {
        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f)
            )
            duration = DURATION_MEDIUM
            interpolator = OvershootInterpolator()
        }
        scaleUp.start()
    }

    /**
     * Animate alpha change for tab selection effect
     */
    fun animateTabAlpha(view: View, targetAlpha: Float, duration: Long = DURATION_SHORT) {
        ObjectAnimator.ofFloat(view, "alpha", view.alpha, targetAlpha).apply {
            this.duration = duration
            start()
        }
    }
}
