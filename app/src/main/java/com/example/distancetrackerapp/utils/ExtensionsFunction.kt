package com.example.distancetrackerapp.utils

import android.view.View
import android.widget.Button

object ExtensionsFunction {

    fun View.animateGone() {
        this.animate().alpha(0f).duration = 1500
    }

    fun View.showAnimate() {
        this.visibility = View.VISIBLE
    }

    fun Button.showAnimate() {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate().alpha(1f).duration = 2500
    }

    fun Button.hide() {
        this.visibility = View.INVISIBLE
    }

    fun Button.disable() {
        this.isEnabled = false
    }

    fun Button.enable() {
        this.isEnabled = true
    }

}