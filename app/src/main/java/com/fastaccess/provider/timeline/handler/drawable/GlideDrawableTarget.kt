package com.fastaccess.provider.timeline.handler.drawable

import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.transition.Transition
import com.fastaccess.R
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

internal class GlideDrawableTarget(
    private val urlDrawable: UrlDrawable,
    private val container: WeakReference<TextView?>?,
    private val width: Int
) : CustomTarget<Drawable?>() {
    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
        if (container?.get() != null) {
            val textView = container.get()
            textView!!.post {
                val width: Float
                val height: Float
                if (resource.intrinsicWidth >= this.width) {
                    val downScale = resource.intrinsicWidth.toFloat() / this.width
                    width = (resource.intrinsicWidth / downScale / 1.3).toFloat()
                    height = (resource.intrinsicHeight / downScale / 1.3).toFloat()
                } else {
                    width = resource.intrinsicWidth.toFloat()
                    height = resource.intrinsicHeight.toFloat()
                }
                val rect = Rect(0, 0, width.roundToInt(), height.roundToInt())
                resource.bounds = rect
                urlDrawable.bounds = rect
                urlDrawable.setDrawable(resource)
                if (resource is GifDrawable) {
                    urlDrawable.callback =
                        textView.getTag(R.id.drawable_callback) as Drawable.Callback?
                    resource.setLoopCount(GifDrawable.LOOP_FOREVER)
                    resource.start()
                } else if (resource is Animatable) {
                    val animate = resource as Animatable
                    urlDrawable.callback =
                        textView.getTag(R.id.drawable_callback) as Drawable.Callback?
                    animate.start()
                }
                textView.text = textView.text
                textView.invalidate()
            }
        }
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        urlDrawable.setDrawable(null)
    }
}