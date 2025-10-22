package com.example.nicemvvmproject.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream

/**
 * @Description:图片加载工具类
 * @Author yanxin
 * @Date:  2025/10/22
 */

class ImageLoader  {

    /** 加载普通图片 */
    fun load(
        context: Context,
        url: Any?,
        imageView: ImageView,
        @DrawableRes placeholder: Int = android.R.color.darker_gray
    ) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /** 加载圆形图片 */
    fun loadCircle(
        context: Context,
        url: Any?,
        imageView: ImageView,
        @DrawableRes placeholder: Int = android.R.color.darker_gray
    ) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .transform(CircleCrop())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /** 加载圆角图片 */
    fun loadRound(
        context: Context,
        url: Any?,
        imageView: ImageView,
        radiusDp: Int,
        @DrawableRes placeholder: Int = android.R.color.darker_gray
    ) {
        val radiusPx = (radiusDp * context.resources.displayMetrics.density).toInt()
        Glide.with(context)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /** 获取 Bitmap（可用于保存到本地） */
    fun loadBitmap(context: Context, url: Any?, onResult: (Bitmap?) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onResult(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    onResult(null)
                }
            })
    }

    /** 保存 Bitmap 到本地文件 */
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** 清除缓存 */
    fun clearCache(context: Context) {
        Glide.get(context).clearMemory()
        Thread { Glide.get(context).clearDiskCache() }.start()
    }
}