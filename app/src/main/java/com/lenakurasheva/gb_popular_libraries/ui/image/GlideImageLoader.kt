package com.lenakurasheva.gb_popular_libraries.ui.image

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lenakurasheva.gb_popular_libraries.mvp.model.cache.IImageCache
import com.lenakurasheva.gb_popular_libraries.mvp.model.image.IImageLoader
import com.lenakurasheva.gb_popular_libraries.mvp.model.network.INetworkStatus
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException

class GlideImageLoader(val imageCache: IImageCache, val networkStatus: INetworkStatus, val uiScheduler: Scheduler): IImageLoader<ImageView> {
    
    override fun loadInto(url: String, container: ImageView) {

        networkStatus.inOnlineSingle().subscribe { isOnline ->
            if (isOnline) {
                Glide.with(container.context)
                    .asBitmap()
                    .load(url)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //Do stuff with error
                            return false
                        }
                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //Do stuff with result
                            imageCache.putImage(url, convertToByteArray(resource))
                                .observeOn(Schedulers.io())
                                .subscribe()
                            return false
                        }
                    })
                    .into(container)
            } else {
                imageCache.getImageBytes(url).observeOn(uiScheduler)
                    .subscribe { result ->
                        Glide.with(container.context)
                            .asBitmap()
                            .load(result)
                            .listener(object : RequestListener<Bitmap> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    //Do stuff with error
                                    return false
                                }
                                override fun onResourceReady(
                                    resource: Bitmap?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    //Do stuff with result
                                    return false
                                }
                            })
                            .into(container)
                    }
            }
        }
    }

    fun convertToByteArray(imageBitmap: Bitmap?): ByteArray {
        val out = ByteArrayOutputStream()
        try {
            imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, out) //100-best quality
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return out.toByteArray()
    }
}
