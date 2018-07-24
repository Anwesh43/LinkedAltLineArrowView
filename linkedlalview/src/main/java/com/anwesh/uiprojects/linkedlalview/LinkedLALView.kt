package com.anwesh.uiprojects.linkedlalview

/**
 * Created by anweshmishra on 25/07/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint

val NODES : Int = 5

fun Canvas.drawLALNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = Math.min(w, h) / (2 * NODES)
    val index : Int = i % 2
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale - 0.5f, 0f)) * 2
    val fc : Float = (1 - sc1) * index + sc1 * (1 - index)
    save()
    translate(i * gap + gap/2, 0f)
    save()
    translate(-gap/4 + gap * sc2, 0f)
    for (j in 0..1) {
        save()
        rotate(30 * (1 - 2 * j) * fc)
        restore()
    }
    restore()
    restore()
}

fun Canvas.drawInAxis(cb : () -> Unit) {
    save()
    translate(width.toFloat() / 2, height.toFloat() / 2)
    for (i in 0..4) {
        save()
        rotate(90f * i)
        cb()
        restore()
    }
    restore()
}

class LinkedLALView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }
}