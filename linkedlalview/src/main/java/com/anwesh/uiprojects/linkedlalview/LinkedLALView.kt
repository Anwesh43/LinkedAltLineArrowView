package com.anwesh.uiprojects.linkedlalview

/**
 * Created by anweshmishra on 25/07/18.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

val NODES : Int = 5

fun Canvas.drawLALNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor("#4CAF50")
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    val gap : Float = Math.min(w, h) / (2 * NODES)
    val index : Int = i % 2
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale - 0.5f, 0f)) * 2
    val fc : Float = (1 - sc1) * index + sc1 * (1 - index)
    save()
    translate(i * gap + gap/2, 0f)
    save()
    translate(gap/4 + gap * sc2, 0f)
    for (j in 0..1) {
        save()
        rotate(30 * (1 - 2 * j) * fc)
        drawLine(0f, 0f, -gap/2, 0f, paint)
        restore()
    }
    restore()
    restore()
}

fun Canvas.drawInOppositeAxis(cb : () -> Unit) {
    save()
    translate(width.toFloat() / 2, height.toFloat() / 2)
    for (i in 0..1) {
        save()
        rotate(180f * i)
        cb()
        restore()
    }
    restore()
}

class LinkedLALView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class LALNode(var i : Int, val state : State = State()) {

        private var next : LALNode? = null

        private var prev : LALNode? = null

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLALNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            state.update {
                stopcb(i, it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < NODES - 1) {
                next = LALNode(i + 1)
                next?.prev = this
            }
        }

        fun getNext(dir : Int, cb : () -> Unit) : LALNode {
            var curr : LALNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedLAL(var i : Int) {

        private var curr : LALNode = LALNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawInOppositeAxis {
                curr.draw(canvas, paint)
            }
        }

        fun update(stopcb : (Int, Float) -> Unit) {
            curr.update {i, scale ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(i, scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedLALView) {

        private val lal : LinkedLAL = LinkedLAL(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lal.draw(canvas, paint)
            animator.animate {
                lal.update {i, scale ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lal.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : LinkedLALView {
            val view : LinkedLALView = LinkedLALView(activity)
            activity.setContentView(view)
            return view
        }
    }
}