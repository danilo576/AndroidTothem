package com.fashiontothem.ff.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.RotateAnimation

object AnimationUtils {
    
    /**
     * Creates a bouncing pulse animation for loading icons
     * Combines scale, alpha, and gentle rotation effects
     */
    fun createBouncingPulseAnimation(): Animation {
        // Create scale animation (bounce effect)
        val scaleAnimation = android.view.animation.ScaleAnimation(
            1.0f, 1.2f, 1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 600
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
            interpolator = BounceInterpolator()
        }
        
        // Create alpha animation (fade in/out)
        val alphaAnimation = AlphaAnimation(0.3f, 1.0f).apply {
            duration = 600
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Create rotation animation (slight tilt)
        val rotationAnimation = RotateAnimation(
            -15f, 15f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 800
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Combine animations
        return AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
            addAnimation(rotationAnimation)
            duration = 800
        }
    }
    
    /**
     * Starts bouncing pulse animation on a view
     */
    fun startBouncingPulseAnimation(view: View) {
        view.startAnimation(createBouncingPulseAnimation())
    }
    
    /**
     * Stops any animation on a view
     */
    fun stopAnimation(view: View) {
        view.clearAnimation()
    }
    
    /**
     * Creates a simple fade in animation
     */
    fun createFadeInAnimation(duration: Long = 300): Animation {
        return AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * Creates a simple fade out animation
     */
    fun createFadeOutAnimation(duration: Long = 300): Animation {
        return AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
    
    /**
     * Creates a scale in animation (zoom in effect)
     */
    fun createScaleInAnimation(duration: Long = 300): Animation {
        return android.view.animation.ScaleAnimation(
            0f, 1f, 0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            this.duration = duration
            interpolator = BounceInterpolator()
        }
    }
    
    /**
     * Creates a scale out animation (zoom out effect)
     */
    fun createScaleOutAnimation(duration: Long = 300): Animation {
        return android.view.animation.ScaleAnimation(
            1f, 0f, 1f, 0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
}
